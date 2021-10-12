package spring.learning.es.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spring.learning.es.model.QUERY_TYPES;
import spring.learning.es.model.RANGE_PARAMETER;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RangeDto {
	private String field;
	private Map<RANGE_PARAMETER, String> params;
}
