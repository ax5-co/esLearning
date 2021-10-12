package spring.learning.es.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import spring.learning.es.dto.AdvancedSearchRequestDto;
import spring.learning.es.dto.SearchRequestDTO;
import spring.learning.es.model.Product;
import spring.learning.es.service.ProductService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;
	
	@PostMapping
	public ResponseEntity<?> add(@RequestBody Product product) {
		productService.index(product);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts() {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService.search(new SearchRequestDTO()));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable String id) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService.getById(id));
	}
	
	@PostMapping("/search/term")
	public ResponseEntity<List<Product>> search(
			@RequestBody(required = false) SearchRequestDTO dto){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService.search(dto));
	}
	
	@PostMapping("/search/price")
	public ResponseEntity<List<Product>> searchByPriceRange(
			@RequestParam(required = false, defaultValue = "0.0") 
			double from, 
			@RequestParam(required = false, defaultValue = ""+Double.MAX_VALUE) 
			double to,
			@RequestParam(required = false) 
			String sortOrder){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService.searchByPriceRange(from, to, sortOrder));
	}
	
	@PostMapping("/search/term/price")
	public ResponseEntity<List<Product>> searchTermByPriceRange(
			@RequestBody(required = false) SearchRequestDTO dto,
			@RequestParam(required = false, defaultValue = "0.0") 
			double from, 
			@RequestParam(required = false, defaultValue = ""+Double.MAX_VALUE) 
			double to,
			@RequestParam(required = false) 
			String sortOrder){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService
						.searchTermByPriceRange(dto, from, to, sortOrder));
	}
	
	@PostMapping("/search/advanced")
	public ResponseEntity<List<Product>> searchAdvancedSearch(
			@RequestBody AdvancedSearchRequestDto dto){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(productService.applyAdvancedSearch(dto));
	}
	
	/**
	 * addDummy() is a method that inserts dummy data into 
	 * the index of 'products' 
	 * Should be run if 'products' index gets recreated to easily populate it
	 */
	@PostMapping("/dummy")
	public ResponseEntity<?> addDummy() {
		productService.insertDummyData();
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
}
