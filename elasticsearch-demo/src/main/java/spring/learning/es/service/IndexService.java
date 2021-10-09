package spring.learning.es.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.learning.es.helper.ElasticsearchIndices;
import spring.learning.es.helper.Utils;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexService {

	private final List<String> INDICES_TO_CREATE =
			List.of(ElasticsearchIndices.PRODUCT_INDEX);
	private final RestHighLevelClient client;

	@Value("${es.indices-mappings-path}")
	private String ES_INDICES_MAPPINGS_PATH;
	@Value("${es.indices-mappings-extension}")
	private String ES_INDICES_MAPPINGS_EXTENSION;
	@Value("${es.indices-settings-path}")
	private String ES_INDICES_SETTINGS_PATH;

	@PostConstruct
	public void tryToCreateIndecies() {
		recreateIndices(false);
	}

	public void recreateIndices(boolean deleteExisting) {
		String settings = loadSettings();
		for (String indexName : INDICES_TO_CREATE) {
			try {
				boolean indexExists = client
						.indices()
						.exists(new GetIndexRequest(indexName),
								RequestOptions.DEFAULT);
				if (indexExists) {
					if (!deleteExisting)
						continue;
					client.indices().delete(
							new DeleteIndexRequest(indexName),
							RequestOptions.DEFAULT);
				}
				final String mappings = loadMappins(indexName);
				if (settings == null || mappings == null) {
					log.error("Failed to create index with name '{}'",
							indexName);
					continue;
				}
				// be careful, since elastic 7.x,
				// use org.elasticsearch.client.indices.CreateIndexRequest
				// not the one from ...admin.action...
				// otherwise, weird errors may occur with no indication of why!
				final CreateIndexRequest createIndexRequest =
						new CreateIndexRequest(indexName);
				createIndexRequest.settings(settings, XContentType.JSON);
				createIndexRequest.mapping(mappings, XContentType.JSON);
				client.indices().create(createIndexRequest,
						RequestOptions.DEFAULT);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private String loadSettings() {
		String settings = Utils.loadAsString(ES_INDICES_SETTINGS_PATH);
		if (settings == null) {
			log.error("Failed to load ES settings");
			return null;
		}
		return settings;
	}

	private String loadMappins(String indexName) {
		String mappings = Utils.loadAsString(
				ES_INDICES_MAPPINGS_PATH 
				+ indexName 
				+ ES_INDICES_MAPPINGS_EXTENSION);
		if (mappings == null) {
			log.error("Failed to load mappings for index with name '{}'",
					indexName);
			return null;
		}
		return mappings;
	}
}
