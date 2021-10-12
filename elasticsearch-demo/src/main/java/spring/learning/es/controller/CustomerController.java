package spring.learning.es.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import spring.learning.es.model.Customer;
import spring.learning.es.service.CustomerService;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;
	
	@PostMapping
	public ResponseEntity<Integer> save(@RequestBody List<Customer> customers) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(customerService.saveAll(customers)) ; 
	}
	
	@GetMapping
	public ResponseEntity<Iterable<Customer>> findAll(){
		return new ResponseEntity<Iterable<Customer>>(customerService
					.findAll(),
			org.springframework.http.HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Customer> findById(@PathVariable String id){
		return new ResponseEntity<Customer>(customerService
					.findById(id),
				org.springframework.http.HttpStatus.OK);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable String id,
			@RequestBody Customer customer){
		return new ResponseEntity<Customer>(customerService
					.updateById(id, customer),
				org.springframework.http.HttpStatus.OK);
	}
	
	@GetMapping("/search")
	public ResponseEntity<List<Customer>> findByFirstname(
			@RequestParam String firstname){
		return new ResponseEntity<List<Customer>>(customerService
					.findAllByFirstname(firstname),
				org.springframework.http.HttpStatus.OK);
	}
}
