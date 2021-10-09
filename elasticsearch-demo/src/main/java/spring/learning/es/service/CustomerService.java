package spring.learning.es.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import spring.learning.es.model.Customer;
import spring.learning.es.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepository;

	public int saveAll(List<Customer> customers) {
		customerRepository.saveAll(customers);
		return customers.size();
	}

	public Iterable<Customer> findAll() {
		return customerRepository.findAll();
	}

	public Customer findById(String id) {
		return customerRepository.findById(id).orElse(null);
	}

	public List<Customer> findAllByFirstname(String firstname) {
		return customerRepository.findAllByFirstname(firstname);
	}

	public Customer updateById(String id, Customer updatedCustomerReq) {
		Optional<Customer> opCustomer = customerRepository.findById(id);
		if (opCustomer.isPresent()) {
			Customer customer= opCustomer.get();
			customer.setFirstname(updatedCustomerReq.getFirstname()!= null ?
					updatedCustomerReq.getFirstname(): customer.getFirstname());
			customer.setLastname(updatedCustomerReq.getLastname()!= null ?
					updatedCustomerReq.getLastname(): customer.getLastname());
			customer.setAge(updatedCustomerReq.getAge() != customer.getAge()?
					updatedCustomerReq.getAge(): customer.getAge());
			customerRepository.save(customer);
		}
		return customerRepository.findById(id).orElse(null);
	}
}
