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
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.repo.DependantRepo;
import com.abcbank.insurance.util.Actor;
import com.abcbank.insurance.util.Text;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DependantService {

	@Autowired
	private DependantRepo repo;
	@Autowired
	private CustomerService cService;
	@Autowired
	private Actor actor;

	public Dependant createDependant(DependantDto dto) {

		Dependant dependant = new Dependant();
		boolean isUpdate = dto.getId() > 0;
		if (isUpdate) {
			dependant = repo.findById(dto.getId());
			if (dependant == null) {
				throw ApiException.notFound("Dependant " + dto.getId() + " was not found.");
			}
		}

		Customer customer = cService.getCustomer(dto.getCustomerId());
		PersonType personType = parsePersonType(dto.getType());

		// Only one NOMINATED beneficiary per customer — the PDF picks the first one,
		// so letting a second one in silently produces the wrong document.
		if (personType == PersonType.NOMINATED || personType == PersonType.BOTH) {
			Dependant existingNominated = repo.findByCustomerAndPersonType(customer, PersonType.NOMINATED)
					.stream().filter(d -> d.getId() != dto.getId()).findFirst().orElse(null);
			if (existingNominated != null) {
				throw ApiException.conflict(
						"This customer already has a nominated beneficiary (" + existingNominated.getName()
								+ "). Edit that record instead of adding a new one.");
			}
		}

		dependant.setId(dto.getId());
		dependant.setPersonType(personType);
		dependant.setName(Text.required(dto.getName(), "Name"));
		dependant.setEmail(Text.orEmpty(dto.getEmail()));
		dependant.setRelationship(Text.required(dto.getRelationship(), "Relationship"));
		dependant.setIdNumber(Text.orEmpty(dto.getIdNumber()));
		dependant.setMobileNumber(Text.orEmpty(dto.getMobileNumber()));
		dependant.setDateOfBirth(dto.getDateOfBirth());
		dependant.setCustomer(customer);
		if (!isUpdate) {
			dependant.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			dependant.setCreatedBy(actor.current());
		} else {
			dependant.setEdittedOn(new Timestamp(System.currentTimeMillis()));
			dependant.setEdittedBy(actor.current());
		}
		log.info("Dependant data saved...");
		dependant = repo.save(dependant);
		syncDependantsCount(customer);
		return dependant;
	}

	public void deleteDependant(int id) {
		Dependant dependant = getDependant(id);
		Customer customer = dependant.getCustomer();
		repo.delete(dependant);
		syncDependantsCount(customer);
	}

	private void syncDependantsCount(Customer customer) {
		int count = (int) repo.findByCustomer(customer).stream()
				.filter(d -> d.getPersonType() == PersonType.DEPENDANT || d.getPersonType() == PersonType.BOTH)
				.count();
		customer.setDependantsNo(count);
		cService.createCustomer(toDto(customer));
	}

	private com.abcbank.insurance.dto.CustomerDto toDto(Customer c) {
		com.abcbank.insurance.dto.CustomerDto dto = new com.abcbank.insurance.dto.CustomerDto();
		dto.setId(c.getId());
		dto.setName(c.getName());
		dto.setDateOfBirth(c.getDateOfBirth() == null ? null : new java.sql.Date(c.getDateOfBirth().getTime()));
		dto.setIdNumber(c.getIdNumber());
		dto.setPinNumber(c.getPinNumber());
		dto.setOccupation(c.getOccupation());
		dto.setGender(c.getGender());
		dto.setMobileNumber(c.getMobileNumber());
		dto.setEmailAddress(c.getEmailAddress());
		dto.setPostalAddress(c.getPostalAddress());
		dto.setPostalCode(c.getPostalCode());
		dto.setCity(c.getCity());
		return dto;
	}

	private PersonType parsePersonType(String type) {
		if (type == null || type.isBlank()) {
			return PersonType.DEPENDANT;
		}
		try {
			return PersonType.valueOf(type.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw ApiException.badRequest("Type must be one of DEPENDANT, BENEFICIARY, NOMINATED or BOTH.");
		}
	}

	public Dependant getDependant(int id) {
		Dependant dependant = repo.findById(id);
		if (dependant == null) {
			throw ApiException.notFound("Dependant " + id + " was not found.");
		}
		return dependant;
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
