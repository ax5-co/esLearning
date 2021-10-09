package spring.learning.es.helper;

import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import lombok.extern.slf4j.Slf4j;
import spring.learning.es.dto.SearchRequestDTO;
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
}
