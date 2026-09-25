package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.CustomerDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.repo.CustomerRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerService {
	
	@Autowired
	private CustomerRepo repo;

	public Customer createCustomer(CustomerDto dto) {
		Customer customer = new Customer();
		if(dto.getId() > 0) {
			customer = repo.findById(dto.getId());
		}
		customer = customer == null ? new Customer() : customer;
		customer.setId(dto.getId());
		customer.setName(dto.getName());
		customer.setGender(dto.getGender());
		customer.setEmailAddress(dto.getEmailAddress());
		customer.setMobileNumber(dto.getMobileNumber());
		customer.setPostalAddress(dto.getPostalAddress());
		customer.setPostalCode(dto.getPostalCode());
		customer.setCity(dto.getCity());
		customer.setIdNumber(dto.getIdNumber());
		customer.setPinNumber(dto.getPinNumber());
		customer.setOccupation(dto.getOccupation());
		customer.setDateOfBirth(dto.getDateOfBirth());
		if(dto.getId() == 0) {
			customer.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		} else {
			customer.setEdittedOn(new Timestamp(System.currentTimeMillis())	);
		}
		log.info("Customer data saved...");
		customer = repo.save(customer);
		return customer;
	}
	
	public List<Customer> getCustomers() {
		return repo.findAll();
	}
	
	public Customer getCustomer(int id) {
		return repo.findById(id);
	}
	
	public Customer getCustomerByPhone(String phoneNumber) {
		return repo.findByMobileNumber(phoneNumber);
	}
	
	public Customer getCustomerByIdNumber(String idNumber) {
		return repo.findByIdNumber(idNumber);
	}
	
	public Customer getCustomerByEmail(String emailAddress) {
		return repo.findByEmailAddress(emailAddress);
	}
}