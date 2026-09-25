package com.abcbank.insurance.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.Customer;

import java.util.List;

@Service
public interface CustomerRepo extends JpaRepository<Customer, Integer> {
	List<Customer> findAll();
	Customer findById(int id);
	Customer findByMobileNumber(String mobileNumber);
	Customer findByIdNumber(String idNumber);
	Customer findByEmailAddress(String emailAddress);
}