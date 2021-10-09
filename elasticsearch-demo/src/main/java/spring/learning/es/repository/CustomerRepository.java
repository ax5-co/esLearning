package spring.learning.es.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import spring.learning.es.model.Customer;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {

	Optional<Customer> findByFirstname(String firstname);
	List<Customer> findAllByFirstname(String firstname);

}
