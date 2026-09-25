package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.CustomerProductDto;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.pdf.ApplicationPDFGenerator;
import com.abcbank.insurance.repo.CustomerProductRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerProductService {
	
	@Autowired
	private CustomerProductRepo repo;
	@Autowired
	private CustomerService cService;
	@Autowired
	private ProductService pService;
	@Autowired
	private ApplicationPDFGenerator applicationPDFGenerator;

	public CustomerProduct createCustomerProduct(CustomerProductDto dto) {
		CustomerProduct customerProduct = new CustomerProduct();
		if(dto.getId() > 0) {
			customerProduct = repo.findById(dto.getId());
		}
		customerProduct = customerProduct == null ? new CustomerProduct() : customerProduct;
		
		customerProduct.setId(dto.getId());
		customerProduct.setCustomer(cService.getCustomer(dto.getCustomerId()));
		customerProduct.setProduct(pService.getProduct(dto.getProductId()));
		customerProduct.setInGoodHealth(dto.isInGoodHealth());
		customerProduct.setHealthStatus(dto.getHealthStatus());
		customerProduct.setSpecificDiasgnosis(dto.isSpecificDiasgnosis());
		customerProduct.setPaymentMade(dto.isPaymentMade());
		if(dto.getId() == 0) {
			customerProduct.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		} else {
			customerProduct.setEdittedOn(new Timestamp(System.currentTimeMillis())	);
		}
		log.info("Customer product data saved...");
		customerProduct = repo.save(customerProduct);
		return customerProduct;
	}
	
	public Customer processCustomerProduct(String email) {
		Customer customer = cService.getCustomerByEmail(email);
		log.info("================ Customer: {}", customer);
		List<CustomerProduct> customerProducts = repo.findByCustomer(customer);
		CustomerProduct customerProduct = null;
		if(customerProducts.size() > 0) {
			customerProduct = customerProducts.get(0);
		}
		//customer.setDependants(dService.getBeneficiaries(customer));
		//customer.setNominatedBeneficiary(dService.getNominatedBeneficiary(customer, PersonType.NOMINATED));
		//customerProduct.setCustomer(customer);
		log.info("================ customerProduct: {}", customerProduct);
		applicationPDFGenerator.generateApplicationPDF(customerProduct);
		return customer;
	}
	
	public CustomerProduct getCustomerProduct(int customerId, int productId) {
		return repo.findByCustomerAndProduct(cService.getCustomer(customerId), pService.getProduct(productId));
	}
	
	public List<CustomerProduct> getCustomerProducts(String email) {
		return repo.findByCustomer(cService.getCustomerByEmail(email));
	}
	
	public List<CustomerProduct> findCustomerProducts() {
		return repo.findAll();
	}
}