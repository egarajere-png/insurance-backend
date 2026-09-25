package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.CustomerProductDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.services.CustomerProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/insurance")
public class CustomerProductController {
	
	@Autowired
	private CustomerProductService cPService;
	
	@PostMapping("/customer-product")
	public CustomerProduct createCustomerProduct(@RequestBody CustomerProductDto dto) {
		return cPService.createCustomerProduct(dto);
	}
	
	@GetMapping("/customer-product/process/{email}")
	public Customer processCustomerProduct(@PathVariable String email) {
		return cPService.processCustomerProduct(email);
	}
	
	@GetMapping("/customer-product/list/{email}")
	public List<CustomerProduct> getCustomerProducts(@PathVariable String email) {
		return cPService.getCustomerProducts(email);
	}
}