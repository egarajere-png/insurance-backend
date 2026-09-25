package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.ProductDto;
import com.abcbank.insurance.entities.Plan;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.repo.ProductRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
	
	@Autowired
	private ProductRepo repo;

	public Product createProduct(ProductDto dto) {
		
		Product product = new Product();
		if(dto.getId() > 0) {
			product = repo.findById(dto.getId());
		}
		product = product == null ? new Product() : product;
		
		Plan plan = dto.getPlan().equalsIgnoreCase("Basic") ? Plan.Basic
				: Plan.Pro;
		product.setId(dto.getId());
		product.setPlan(plan);
		product.setName(dto.getName());
		product.setDescription(dto.getDescription());
		product.setBenefit(dto.getBenefit());
		product.setBenefitChild(dto.getBenefitChild());
		product.setPremium(dto.getPremium());
		product.setPremiumAdditionalChild(dto.getPremiumAdditionalChild());
		product.setPremiumAdditionalAdultChild(dto.getPremiumAdditionalAdultChild());
		if(dto.getId() == 0) {
			product.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		} else {
			product.setEdittedOn(new Timestamp(System.currentTimeMillis())	);
		}
		log.info("Product data saved...");
		product = repo.save(product);
		return product;
	}
	
	public Product getProduct(int id) {
		return repo.findById(id);
	}
	
	public List<Product> getProducts() {
		return repo.findAll();
	}
}