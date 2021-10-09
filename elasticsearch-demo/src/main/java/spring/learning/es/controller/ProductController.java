package spring.learning.es.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import spring.learning.es.dto.SearchRequestDTO;
import spring.learning.es.model.Product;
import spring.learning.es.service.ProductService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;
	
	@PostMapping
	public void add(@RequestBody Product product) {
		productService.index(product);
	}
	
	@GetMapping
	public List<Product> getAllProducts() {
		return productService.search(new SearchRequestDTO());
	}
	
	@GetMapping("/{id}")
	public Product getById(@PathVariable String id) {
		return productService.getById(id);
	}
	
	@PostMapping("/search/term")
	public List<Product> search(
			@RequestBody(required = false) SearchRequestDTO dto){
		return productService.search(dto);
	}
	
	@PostMapping("/search/price")
	public List<Product> searchByPriceRange(
			
			@RequestParam(required = false, defaultValue = "0.0") 
			double from, 
			@RequestParam(required = false, defaultValue = ""+Double.MAX_VALUE) 
			double to,
			@RequestParam(required = false) 
			String sortOrder){
		return productService.searchByPriceRange(from, to, sortOrder);
	}
	
	@PostMapping("/search/term/price")
	public List<Product> searchTermByPriceRange(
			@RequestBody(required = false) SearchRequestDTO dto,
			@RequestParam(required = false, defaultValue = "0.0") 
			double from, 
			@RequestParam(required = false, defaultValue = ""+Double.MAX_VALUE) 
			double to,
			@RequestParam(required = false) 
			String sortOrder){
		return productService.searchTermByPriceRange(dto, from, to, sortOrder);
	}
	
	/**
	 * addDummy() is a method that inserts dummy data into 
	 * the index of 'products' 
	 * It should be run if 'products' index gets recreated to easily populate it
	 */
	@PostMapping("/dummy")
	public void addDummy() {
		productService.insertDummyData();
	}
}
