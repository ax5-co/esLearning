package spring.learning.es.dto;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO {
	private String searchTerm;
	private List<String> fields;
	private String sortBy;
	private SortOrder sortOrder;

}
