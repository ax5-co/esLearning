package spring.learning.es.dto;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvancedSearchRequestDto {
	private List<MatchDto> mustMatch;
	private List<MatchDto> mustNotMatch;
	private List<MatchDto> shouldMatch;
	private List<RangeDto> mustRange;
	private List<RangeDto> mustNotRange;
	private List<RangeDto> shouldRange;
	private List<SortDto> sortBy;
}
