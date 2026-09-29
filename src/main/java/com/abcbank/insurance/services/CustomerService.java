package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.CustomerDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.repo.CustomerRepo;
import com.abcbank.insurance.util.Actor;
import com.abcbank.insurance.util.Text;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerService {

	@Autowired
	private CustomerRepo repo;
	@Autowired
	private Actor actor;

	public Customer createCustomer(CustomerDto dto) {
		Customer customer = new Customer();
		boolean isUpdate = dto.getId() > 0;
		if (isUpdate) {
			customer = repo.findById(dto.getId());
			if (customer == null) {
				throw ApiException.notFound("Customer " + dto.getId() + " was not found.");
			}
		}

		// Email/ID number must stay unique. On update, an owned match is fine.
		Customer byEmail = repo.findByEmailAddress(dto.getEmailAddress());
		if (byEmail != null && byEmail.getId() != dto.getId()) {
			throw ApiException.conflict("A customer with this email address already exists.");
		}
		Customer byIdNumber = repo.findByIdNumber(dto.getIdNumber());
		if (byIdNumber != null && byIdNumber.getId() != dto.getId()) {
			throw ApiException.conflict("A customer with this ID number already exists.");
		}

		customer.setId(dto.getId());
		customer.setName(Text.required(dto.getName(), "Name"));
		customer.setGender(Text.normalizeGender(dto.getGender()));
		customer.setEmailAddress(Text.required(dto.getEmailAddress(), "Email address"));
		customer.setMobileNumber(Text.required(dto.getMobileNumber(), "Mobile number"));
		customer.setPostalAddress(Text.orEmpty(dto.getPostalAddress()));
		customer.setPostalCode(Text.orEmpty(dto.getPostalCode()));
		customer.setCity(Text.orEmpty(dto.getCity()));
		customer.setIdNumber(Text.required(dto.getIdNumber(), "ID number"));
		customer.setPinNumber(Text.orEmpty(dto.getPinNumber()));
		customer.setOccupation(Text.orEmpty(dto.getOccupation()));
		customer.setDateOfBirth(dto.getDateOfBirth());
		if (!isUpdate) {
			customer.setDependantsNo(0);
			customer.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			customer.setCreatedBy(actor.current());
		} else {
			customer.setEdittedOn(new Timestamp(System.currentTimeMillis()));
			customer.setEdittedBy(actor.current());
		}
		log.info("Customer data saved...");
		customer = repo.save(customer);
		return customer;
	}

	public List<Customer> getCustomers() {
		return repo.findAll();
	}

	public Customer getCustomer(int id) {
		Customer customer = repo.findById(id);
		if (customer == null) {
			throw ApiException.notFound("Customer " + id + " was not found.");
		}
		return customer;
	}

	public Customer getCustomerByPhone(String phoneNumber) {
		Customer customer = repo.findByMobileNumber(phoneNumber);
		if (customer == null) {
			throw ApiException.notFound("No customer found with mobile number " + phoneNumber + ".");
		}
		return customer;
	}

	public Customer getCustomerByIdNumber(String idNumber) {
		Customer customer = repo.findByIdNumber(idNumber);
		if (customer == null) {
			throw ApiException.notFound("No customer found with ID number " + idNumber + ".");
		}
		return customer;
	}

	public Customer getCustomerByEmail(String emailAddress) {
		Customer customer = repo.findByEmailAddress(emailAddress);
		if (customer == null) {
			throw ApiException.notFound("No customer found with email " + emailAddress + ".");
		}
		return customer;
	}
}
