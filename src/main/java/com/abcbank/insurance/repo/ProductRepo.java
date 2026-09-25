package com.abcbank.insurance.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.Product;

import java.util.List;

@Service
public interface ProductRepo extends JpaRepository<Product, Integer> {
	Product findById(int id);
	List<Product> findAll();
}