package com.abcbank.insurance.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.Dependant;
import com.abcbank.insurance.entities.PersonType;

import java.util.List;

@Service
public interface DependantRepo extends JpaRepository<Dependant, Integer> {
	Dependant findById(int id);
	List<Dependant> findAll();
	List<Dependant> findByCustomer(Customer customer);
	List<Dependant> findByCustomerAndPersonType(Customer customer, PersonType personType);
	Customer findByIdNumber(String idNumber);
}