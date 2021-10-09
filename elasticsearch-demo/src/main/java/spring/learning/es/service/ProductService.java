package spring.learning.es.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.learning.es.dto.SearchRequestDTO;
import spring.learning.es.helper.ElasticsearchIndices;
import spring.learning.es.helper.SearchUtils;
import spring.learning.es.model.Product;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final RestHighLevelClient client;
	private static final String PRICE_FIELD_NAME = "price";
	@Value("${dummy.data.json.product.path}")
	private String dummyDataPath;
	public boolean index(final Product product) {
		try {
			final String productAsString = MAPPER.writeValueAsString(product);
			IndexRequest request = 
					new IndexRequest(ElasticsearchIndices.PRODUCT_INDEX);
			request.id(product.getId());
			request.source(productAsString, XContentType.JSON);
			IndexResponse response = client.index(request,
					RequestOptions.DEFAULT);
			
			return response != null && response.status().equals(RestStatus.OK);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			return false;
		}catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
	public Product getById(String productId) {
		try {
			GetResponse documentFields = client
					.get(new GetRequest(ElasticsearchIndices.PRODUCT_INDEX,
							productId),
						RequestOptions.DEFAULT);
			
			if (documentFields == null || documentFields.isSourceEmpty())
				return null;
			
			return MAPPER.readValue(documentFields.getSourceAsString(),
					Product.class);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public List<Product> search(SearchRequestDTO esSearchRequestDTO){
		SearchRequest request = SearchUtils
				.createSearchRequest(esSearchRequestDTO,
						ElasticsearchIndices.PRODUCT_INDEX);
		return searchInternal(request); 
	}
	
	public List<Product> searchByPriceRange(double priceFrom, double priceTo,
			String sortOrder){
		SearchRequest request = SearchUtils
				.createSearchRequest(
						ElasticsearchIndices.PRODUCT_INDEX,
						PRICE_FIELD_NAME, priceFrom, priceTo, sortOrder);
		return searchInternal(request); 
	}

	public List<Product> searchTermByPriceRange(SearchRequestDTO dto,
			double priceFrom, double priceTo, String sortOrder) {
		SearchRequest request = SearchUtils
				.createSearchRequest(
						dto, ElasticsearchIndices.PRODUCT_INDEX,
						PRICE_FIELD_NAME, priceFrom, priceTo, sortOrder);
		return searchInternal(request); 
	}
	
	private List<Product> searchInternal(SearchRequest request) {
		try {
			return Arrays
				.asList(client
					.search(request, RequestOptions.DEFAULT)
					.getHits()
					.getHits())
				.stream()
				.map(hit -> {
					try {
						return MAPPER.readValue(hit.getSourceAsString(),
							Product.class);
					}catch (JsonMappingException e) {
						log.error(e.getMessage(), e);
						return null;
					}catch (JsonProcessingException e) {
						log.error(e.getMessage(), e);
						return null;
					}}
				).collect(Collectors.toList());				
		}catch (IOException e) {
			log.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}
	
	public void insertDummyData() {
		try {
			File file = new File(dummyDataPath);
			InputStream inputStream = new FileInputStream(file);
			TypeReference<List<Product>> typeReference = new TypeReference<List<Product>>(){};
			List<Product> products = MAPPER.readValue(inputStream, typeReference);
			products.forEach(product -> index(product));	
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		}catch (JsonParseException e) {
			log.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
