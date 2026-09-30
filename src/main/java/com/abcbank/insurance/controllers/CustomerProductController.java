package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.CustomerProductDto;
import com.abcbank.insurance.dto.DashboardStatsDto;
import com.abcbank.insurance.dto.ReviewDto;
import com.abcbank.insurance.entities.ApplicationStatus;
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
	public CustomerProduct createCustomerProduct(
			@RequestBody CustomerProductDto dto) {

		return cPService.createCustomerProduct(dto);
	}

	/**
	 * Audit / applications table.
	 *
	 * Optional:
	 * ?status=PENDING_REVIEW
	 * ?status=APPROVED
	 * ?status=REJECTED
	 *
	 * Used by the dashboard's application status filters.
	 */
	@GetMapping("/customer-product/list")
	public List<CustomerProduct> listApplications(
			@RequestParam(required = false)
			ApplicationStatus status) {

		return status == null
				? cPService.findAll()
				: cPService.findByStatus(status);
	}

	@GetMapping("/customer-product/dashboard-stats")
	public DashboardStatsDto getDashboardStats() {

		return cPService.getDashboardStats();
	}

	@GetMapping("/customer-product/{id}")
	public CustomerProduct getApplication(
			@PathVariable int id) {

		return cPService.getById(id);
	}

	/**
	 * Admin accepts the application.
	 *
	 * From this point the PDF can be generated/downloaded.
	 */
	@PostMapping("/customer-product/{id}/approve")
	public CustomerProduct approve(
			@PathVariable int id,
			@RequestBody(required = false) ReviewDto body) {

		return cPService.approve(
				id,
				body == null
						? null
						: body.getNotes()
		);
	}

	/**
	 * Admin declines the application.
	 *
	 * A reason is required.
	 */
	@PostMapping("/customer-product/{id}/reject")
	public CustomerProduct reject(
			@PathVariable int id,
			@RequestBody ReviewDto body) {

		return cPService.reject(
				id,
				body.getNotes()
		);
	}

	/**
	 * Kept for backward compatibility with the old process trigger
	 * used by the UI.
	 *
	 * PDF generation now happens during approval and on demand
	 * through the PDF endpoints.
	 */
	@GetMapping("/customer-product/process/{email}")
	public Customer processCustomerProduct(
			@PathVariable String email) {

		return cPService
				.getCustomerProducts(email)
				.stream()
				.findFirst()
				.map(CustomerProduct::getCustomer)
				.orElse(null);
	}

	@GetMapping("/customer-product/list/{email}")
	public List<CustomerProduct> getCustomerProducts(
			@PathVariable String email) {

		return cPService.getCustomerProducts(email);
	}

	/**
	 * Preferred PDF download route.
	 *
	 * Uses the application ID and only allows APPROVED applications.
	 */
	@GetMapping("/customer-product/{id}/pdf")
	public ResponseEntity<byte[]> downloadApplicationPdfById(
			@PathVariable int id)
			throws java.io.IOException {

		byte[] pdf =
				cPService.downloadApplicationPdf(id);

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(pdf.length)
				.header(
						HttpHeaders.CONTENT_DISPOSITION,
						"inline; filename=\"application-"
								+ id
								+ ".pdf\""
				)
				.body(pdf);
	}

	/**
	 * Legacy email-based PDF route.
	 *
	 * Returns the latest approved application for the customer.
	 */
	@GetMapping("/customer-product/pdf/{email}")
	public ResponseEntity<byte[]> downloadApplicationPdf(
			@PathVariable String email)
			throws java.io.IOException {

		byte[] pdf =
				cPService.downloadApplicationPdf(email);

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(pdf.length)
				.header(
						HttpHeaders.CONTENT_DISPOSITION,
						"inline; filename=\""
								+ email
								+ "-application.pdf\""
				)
				.body(pdf);
	}
}