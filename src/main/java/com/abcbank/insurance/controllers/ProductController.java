package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.ProductDto;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.services.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/insurance")
public class ProductController {
	
	@Autowired
	private ProductService pService;
	
	@PostMapping("/product")
	public Product createProduct(@RequestBody ProductDto productDto) {
		return pService.createProduct(productDto);
	}
	
	@GetMapping("/product/{id}")
	public Product getProduct(@PathVariable int id) {
		return pService.getProduct(id);
	}
	
	@GetMapping("/product/list")
	public List<Product> getProducts() {
		return pService.getProducts();
	}
}