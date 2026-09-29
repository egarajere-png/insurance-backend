package com.abcbank.insurance.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.ApplicationStatus;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Product;

@Service
public interface CustomerProductRepo extends JpaRepository<CustomerProduct, Integer> {
	List<CustomerProduct>  findAll();
	CustomerProduct findById(int id);
	CustomerProduct findByCustomerAndProduct(Customer customer, Product product);
	List<CustomerProduct> findByCustomer(Customer customer);

	List<CustomerProduct> findAllByOrderByCreatedOnDesc();
	List<CustomerProduct> findByStatusOrderByCreatedOnDesc(ApplicationStatus status);
	List<CustomerProduct> findByCustomerOrderByCreatedOnDesc(Customer customer);
	List<CustomerProduct> findByCustomerAndStatusOrderByReviewedOnDesc(Customer customer, ApplicationStatus status);
	List<CustomerProduct> findByCustomerAndProductAndStatusIn(Customer customer, Product product, Collection<ApplicationStatus> statuses);
	long countByStatus(ApplicationStatus status);
	boolean existsByProduct(Product product);
}
