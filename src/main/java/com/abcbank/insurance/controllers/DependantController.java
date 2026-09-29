package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.DependantDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.Dependant;
import com.abcbank.insurance.services.CustomerService;
import com.abcbank.insurance.services.DependantService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/insurance")
public class DependantController {

	@Autowired
	private DependantService dService;
	@Autowired
	private CustomerService cService;

	@PostMapping("/dependant")
	public Dependant createDependant(@RequestBody DependantDto dependantDto) {
		return dService.createDependant(dependantDto);
	}

	/** Edit an existing dependant/beneficiary record. */
	@PutMapping("/dependant/{id}")
	public Dependant updateDependant(@PathVariable int id, @RequestBody DependantDto dependantDto) {
		dependantDto.setId(id);
		return dService.createDependant(dependantDto);
	}

	@DeleteMapping("/dependant/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDependant(@PathVariable int id) {
		dService.deleteDependant(id);
	}

	@GetMapping("/dependant/{id}")
	public Dependant getDependant(@PathVariable int id) {
		return dService.getDependant(id);
	}

	@GetMapping("/beneficiary/list/{customerId}")
	public List<Dependant> getBeneficiaries(@PathVariable int customerId) {
		Customer customer = cService.getCustomer(customerId);
		return dService.getBeneficiaries(customer);
	}

	@GetMapping("/beneficiary/list/email/{emailAddress}")
	public List<Dependant> getBeneficiariesByEmail(@PathVariable String emailAddress) {
		Customer customer = cService.getCustomerByEmail(emailAddress);
		return dService.getBeneficiaries(customer);
	}

	@GetMapping("/dependant/list")
	public List<Dependant> getDependants() {
		return dService.getDependants();
	}

	@GetMapping("/customer/dependant/list/{customerId}")
	public List<Dependant> getCustomerDependants(@PathVariable int customerId) {
		return dService.getCustomerDependants(customerId);
	}
}
