package com.abcbank.insurance.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Product;

import java.util.List;

@Service
public interface CustomerProductRepo extends JpaRepository<CustomerProduct, Integer> {
	List<CustomerProduct>  findAll();
	CustomerProduct findById(int id);
	CustomerProduct findByCustomerAndProduct(Customer customer, Product product);
	List<CustomerProduct> findByCustomer(Customer customer);
}