package spring.learning.es.controller;

import spring.learning.es.service.IndexService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/index")
public class IndexController {

	private final IndexService indexService;
	
	@PostMapping("/recreate")
	public void recreateAllIndeices(
			@RequestParam (name = "delete_existing",
				required = false, defaultValue = "true")
			boolean deleteExisting) {
		indexService.recreateIndices(deleteExisting);
	}
}
