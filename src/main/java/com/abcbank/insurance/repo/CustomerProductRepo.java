package com.abcbank.insurance.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abcbank.insurance.entities.ApplicationStatus;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Product;

@Repository
public interface CustomerProductRepo extends JpaRepository<CustomerProduct, Integer> {

	/**
	 * CustomerProduct has relationships that Hibernate keeps lazy by default.
	 * The REST API returns CustomerProduct objects directly, so these related
	 * records must be initialized before the Hibernate session closes.
	 */
	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findAll();

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	CustomerProduct findById(int id);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	CustomerProduct findByCustomerAndProduct(Customer customer, Product product);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findByCustomer(Customer customer);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findAllByOrderByCreatedOnDesc();

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findByStatusOrderByCreatedOnDesc(ApplicationStatus status);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findByCustomerOrderByCreatedOnDesc(Customer customer);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findByCustomerAndStatusOrderByReviewedOnDesc(
			Customer customer,
			ApplicationStatus status
	);

	@EntityGraph(attributePaths = {
			"customer",
			"product",
			"coveredPeople",
			"coveredPeople.customer"
	})
	List<CustomerProduct> findByCustomerAndProductAndStatusIn(
			Customer customer,
			Product product,
			Collection<ApplicationStatus> statuses
	);

	long countByStatus(ApplicationStatus status);

	boolean existsByProduct(Product product);
}