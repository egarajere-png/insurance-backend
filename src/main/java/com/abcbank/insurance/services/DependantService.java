package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.DependantDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.Dependant;
import com.abcbank.insurance.entities.PersonType;
import com.abcbank.insurance.repo.DependantRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DependantService {
	
	@Autowired
	private DependantRepo repo;
	@Autowired
	private CustomerService cService;

	public Dependant createDependant(DependantDto dto) {
		
		Dependant dependant = new Dependant();
		if(dto.getId() > 0) {
			dependant = repo.findById(dto.getId());
		}
		dependant = dependant == null ? new Dependant() : dependant;
		
		PersonType personType = dto.getType().equalsIgnoreCase("Beneficiary") ? PersonType.BENEFICIARY
				: dto.getType().equalsIgnoreCase("Dependant") ? PersonType.DEPENDANT
						: dto.getType().equalsIgnoreCase("Nominated") ? PersonType.NOMINATED : PersonType.BOTH;
		dependant.setId(dto.getId());
		dependant.setPersonType(personType);
		dependant.setName(dto.getName());
		dependant.setEmail(dto.getEmail());
		dependant.setRelationship(dto.getRelationship());
		dependant.setIdNumber(dto.getIdNumber());
		dependant.setMobileNumber(dto.getMobileNumber());
		dependant.setDateOfBirth(dto.getDateOfBirth());
		dependant.setCustomer(cService.getCustomer(dto.getCustomerId()));
		if(dto.getId() == 0) {
			dependant.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		} else {
			dependant.setEdittedOn(new Timestamp(System.currentTimeMillis()));
		}
		log.info("Dependant data saved...");
		dependant = repo.save(dependant);
		return dependant;
	}
	
	public Dependant getDependant(int id) {
		return repo.findById(id);
	}
	
	public List<Dependant> getBeneficiaries(Customer customer) {
		List<Dependant> beneficiaries = repo.findByCustomerAndPersonType(customer, PersonType.BENEFICIARY);
		return beneficiaries.size() > 0 ? beneficiaries : new ArrayList<>();
	}
	
	public Dependant getNominatedBeneficiary(Customer customer, PersonType personType) {
		List<Dependant> beneficiaries = repo.findByCustomerAndPersonType(customer, personType);
		return beneficiaries.size() > 0 ? beneficiaries.get(0) : null;
	}
	
	public List<Dependant> getDependants() {
		return repo.findAll();
	}
	
	public List<Dependant> getCustomerDependants(Customer customer) {
		return repo.findByCustomer(customer);
	}
	
	public List<Dependant> getCustomerDependants(int customerId) {
		return repo.findByCustomer(cService.getCustomer(customerId));
	}
}