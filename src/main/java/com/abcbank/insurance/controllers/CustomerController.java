package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.CustomerDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.services.CustomerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/insurance")
public class CustomerController {
	
	@Autowired
	private CustomerService cService;
	
	@PostMapping("/customer")
	public Customer createCustomer(@RequestBody CustomerDto customerDto) {
		return cService.createCustomer(customerDto);
	}
	
	@GetMapping("/customer/list")
	public List<Customer> getCustomers() {
		return cService.getCustomers();
	}
	
	@GetMapping("/customer/email/{email}")
	public Customer getCustomer(@PathVariable String email) {
		return cService.getCustomerByEmail(email);
	}
	
	@GetMapping("/customer/id-number/{idNumber}")
	public Customer getCustomerByIdNumber(@PathVariable String idNumber) {
		return cService.getCustomerByIdNumber(idNumber);
	}
	
	@GetMapping("/customer/phone-number/{phoneNumber}")
	public Customer getCustomerByPhone(@PathVariable String phoneNumber) {
		return cService.getCustomerByPhone(phoneNumber);
	}
}