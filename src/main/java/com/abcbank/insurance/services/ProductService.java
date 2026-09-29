package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.ProductDto;
import com.abcbank.insurance.entities.Plan;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.repo.CustomerProductRepo;
import com.abcbank.insurance.repo.ProductRepo;
import com.abcbank.insurance.util.Actor;
import com.abcbank.insurance.util.Text;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

	@Autowired
	private ProductRepo repo;
	@Autowired
	private CustomerProductRepo customerProductRepo;
	@Autowired
	private Actor actor;

	public Product createProduct(ProductDto dto) {

		Product product = new Product();
		boolean isUpdate = dto.getId() > 0;
		if (isUpdate) {
			product = repo.findById(dto.getId());
			if (product == null) {
				throw ApiException.notFound("Product " + dto.getId() + " was not found.");
			}
		}

		product.setId(dto.getId());
		product.setPlan(parsePlan(dto.getPlan()));
		product.setName(Text.required(dto.getName(), "Product name"));
		product.setDescription(Text.required(dto.getDescription(), "Description"));
		product.setBenefit(nonNegative(dto.getBenefit(), "Benefit"));
		product.setBenefitChild(nonNegative(dto.getBenefitChild(), "Child benefit"));
		product.setPremium(nonNegative(dto.getPremium(), "Premium"));
		product.setPremiumAdditionalChild(nonNegative(dto.getPremiumAdditionalChild(), "Additional child premium"));
		product.setPremiumAdditionalAdultChild(
				nonNegative(dto.getPremiumAdditionalAdultChild(), "Additional adult child premium"));
		if (!isUpdate) {
			product.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			product.setCreatedBy(actor.current());
		} else {
			product.setEdittedOn(new Timestamp(System.currentTimeMillis()));
			product.setEdittedBy(actor.current());
		}
		log.info("Product data saved...");
		product = repo.save(product);
		return product;
	}

	public void deleteProduct(int id) {
		Product product = getProduct(id);
		if (customerProductRepo.existsByProduct(product)) {
			throw ApiException.conflict("This product has applications on file and cannot be deleted. Consider editing it instead.");
		}
		repo.delete(product);
	}

	private int nonNegative(int value, String label) {
		if (value < 0) {
			throw ApiException.badRequest(label + " cannot be negative.");
		}
		return value;
	}

	private Plan parsePlan(String plan) {
		if (plan == null || plan.isBlank()) {
			throw ApiException.badRequest("Plan is required (Basic or Pro).");
		}
		if (plan.equalsIgnoreCase("Basic")) {
			return Plan.Basic;
		}
		if (plan.equalsIgnoreCase("Pro")) {
			return Plan.Pro;
		}
		throw ApiException.badRequest("Plan must be Basic or Pro.");
	}

	public Product getProduct(int id) {
		Product product = repo.findById(id);
		if (product == null) {
			throw ApiException.notFound("Product " + id + " was not found.");
		}
		return product;
	}

	public List<Product> getProducts() {
		return repo.findAll();
	}
}
