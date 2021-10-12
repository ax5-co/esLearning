package spring.learning.es.helper;

import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import lombok.extern.slf4j.Slf4j;
import spring.learning.es.dto.AdvancedSearchRequestDto;
import spring.learning.es.dto.MatchDto;
import spring.learning.es.dto.RangeDto;
import spring.learning.es.dto.SearchRequestDTO;
import spring.learning.es.model.QUERY_TYPES;
import spring.learning.es.model.RANGE_PARAMETER;
@Slf4j
public class SearchUtils {

	public static SearchRequest createSearchRequest(SearchRequestDTO dto, 
			String indexName) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				.postFilter(createQueryBuilder(dto));
		if (dto.getSortBy() != null) {
			searchSourceBuilder.sort(
					dto.getSortBy(), 
					dto.getSortOrder() != null ? dto.getSortOrder() 
							: SortOrder.ASC
			);
		}
			
		return new SearchRequest(indexName)
				.source(searchSourceBuilder);
	}
	
	public static SearchRequest createSearchRequest(
			String indexName,
			String field, double priceFrom, double priceTo,
			String sortOrder) {
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				.postFilter(createQueryBuilder(field, priceFrom, priceTo));	
		try{
			searchSourceBuilder.sort(
					field,
					sortOrder != null ? SortOrder.fromString(sortOrder)
							: SortOrder.ASC);
		}catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}	
		return new SearchRequest(indexName)
				.source(searchSourceBuilder);
	}
	
	public static SearchRequest createSearchRequest(SearchRequestDTO dto,
			String indexName,
			String field, double priceFrom, double priceTo, String sortOrder) {
		QueryBuilder priceRangeQuery = createQueryBuilder(field, priceFrom,
				priceTo);
		QueryBuilder searchTermQuery = createQueryBuilder(dto);
		QueryBuilder boolQuery = QueryBuilders.boolQuery()
				.must(searchTermQuery)
				.must(priceRangeQuery);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				.postFilter(boolQuery);	
		try{
			searchSourceBuilder.sort(
					field,
					sortOrder != null ? SortOrder.fromString(sortOrder)
							: SortOrder.ASC);
		}catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}	
		return new SearchRequest(indexName)
				.source(searchSourceBuilder);
	}
	
	private static QueryBuilder createQueryBuilder (SearchRequestDTO dto) {
		//if nothing is fed as a searchTerm/fields,
		//then it means return All documents
		if(dto == null 
			|| dto.getSearchTerm()== null
			|| dto.getFields()== null
			|| dto.getFields().isEmpty()
			|| dto.getSearchTerm().isEmpty()) {
			return QueryBuilders.matchAllQuery();
		}
		final List<String> fields = dto.getFields();
		
		//multimatch case
		if(fields.size() > 1) {
			MultiMatchQueryBuilder queryBuilder = QueryBuilders
					.multiMatchQuery(dto.getSearchTerm())
					.type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
					.operator(Operator.AND);
			
			fields.forEach(queryBuilder::field);
			return queryBuilder;
		}
		//1 field match case
		return fields.stream()
				.findFirst()
				.map(field -> QueryBuilders
						.matchQuery(field, dto.getSearchTerm())
						.operator(Operator.AND))
				.orElse(null);
	}
	
	private static QueryBuilder createQueryBuilder (String field,
			double from, double to) {
		return QueryBuilders.rangeQuery(field).gte(from).lte(to);
	}
	
	//Advanced search
	public static SearchRequest createSearchRequest(
			AdvancedSearchRequestDto dto,
			String indexName) {
		if (nullDTO(dto)) {
			return new SearchRequest(indexName)
					.source(new SearchSourceBuilder() 
							.postFilter(matchAllQueryBuilder()));
		}
		List<QueryBuilder> mustMatchQueries = createMatchQueryBuilder (
				dto.getMustMatch() != null? dto.getMustMatch(): null );
		List<QueryBuilder> mustNotMatchQueries = createMatchQueryBuilder (
				dto.getMustNotMatch() != null? dto.getMustNotMatch(): null );
		List<QueryBuilder> shouldMatchQueries = createMatchQueryBuilder (
				dto.getShouldMatch() != null? dto.getShouldMatch(): null );
		List<QueryBuilder> mustRangeQueries = createRangeQueryBuilder (
				dto.getMustRange() != null? dto.getMustRange(): null );
		List<QueryBuilder> mustNotRangeQueries = createRangeQueryBuilder (
				dto.getMustNotRange() != null? dto.getMustNotRange(): null );
		List<QueryBuilder> shouldRangeQueries = createRangeQueryBuilder (
				dto.getShouldRange() != null? dto.getShouldRange(): null );
		
		QueryBuilder boolQuery = QueryBuilders.boolQuery()
				.must(reduceQueryBuilders(mustMatchQueries,
						QUERY_TYPES.MUST))
				.mustNot(reduceQueryBuilders(mustNotMatchQueries,
						QUERY_TYPES.MUST_NOT))
				.should(reduceQueryBuilders(shouldMatchQueries,
						QUERY_TYPES.SHOULD))
				.filter(QueryBuilders.boolQuery()
						.must(reduceQueryBuilders(mustRangeQueries,
								QUERY_TYPES.MUST))
						.mustNot(reduceQueryBuilders(mustNotRangeQueries,
								QUERY_TYPES.MUST_NOT))
						.should(reduceQueryBuilders(shouldRangeQueries,
								QUERY_TYPES.SHOULD)));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				.postFilter(boolQuery);
		List<SortBuilder<?>> sortBuilders = 
		dto.getSortBy()
				.stream()
				.map(sortDto -> SortBuilders
						.fieldSort(sortDto.getField())
						.order(sortDto.getOrder()))
				.collect(Collectors.toList());
		
		return new SearchRequest(indexName)
				.source(searchSourceBuilder
						.sort(sortBuilders));
	}

	private static QueryBuilder matchAllQueryBuilder() {
		return QueryBuilders.matchAllQuery();
	}

	private static boolean nullDTO(AdvancedSearchRequestDto dto) {
		if(dto == null 
				|| (dto.getMustMatch()== null
				&& dto.getMustNotMatch()== null
				&& dto.getShouldMatch()== null
				&& dto.getMustRange()== null
				&& dto.getMustNotRange()== null
				&& dto.getShouldRange()== null) ) {
			return true;
			}
		return false;
	}

	private static List<QueryBuilder> createMatchQueryBuilder(
			List<MatchDto> matchDtos) {
		return matchDtos
				.stream()
				.map(dto -> {
					MultiMatchQueryBuilder queryBuilder = QueryBuilders
							.multiMatchQuery(dto.getQuery())
							.type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
							.operator(Operator.AND);
					
					dto.getFields().forEach(queryBuilder::field);

					return queryBuilder;
				})
				.collect(Collectors.toList());
	}
	
	private static List<QueryBuilder> createRangeQueryBuilder(
			List<RangeDto> rangeDtos) {
		return rangeDtos
				.stream()
				.map(SearchUtils::getRangeQuery)
				.collect(Collectors.toList());	 
	}

	private static QueryBuilder getRangeQuery (RangeDto dto) {
		return QueryBuilders
		.rangeQuery(dto.getField())
		.gt(dto.getParams()
				.getOrDefault(RANGE_PARAMETER.GT, null))
		.gte(dto.getParams()
				.getOrDefault(RANGE_PARAMETER.GTE, null))
		.lt(dto.getParams()
				.getOrDefault(RANGE_PARAMETER.LT, null))
		.lte(dto.getParams()
				.getOrDefault(RANGE_PARAMETER.LTE, null));

	}
	
	private static QueryBuilder reduceQueryBuilders(
			List<QueryBuilder> builders,
			QUERY_TYPES queryType) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		//the mustNot query will be negated (must -> mustNot) 
		//in createSearchRequest()
		if (queryType == QUERY_TYPES.MUST 
				|| queryType == QUERY_TYPES.MUST_NOT) {
			for (QueryBuilder builder : builders){
				boolQuery.must(builder);
			}
		} else if (queryType == QUERY_TYPES.SHOULD) {
			for (QueryBuilder builder : builders){
				boolQuery.should(builder);
			}
		}
		return boolQuery;
	}
}
