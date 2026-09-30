package com.abcbank.insurance.services;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.CustomerProductDto;
import com.abcbank.insurance.dto.DashboardStatsDto;
import com.abcbank.insurance.entities.ApplicationStatus;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.pdf.ApplicationPDFGenerator;
import com.abcbank.insurance.repo.CustomerProductRepo;
import com.abcbank.insurance.util.Actor;

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

	@Autowired
	private Actor actor;

	/**
	 * Customer finishes/submits their application.
	 *
	 * New applications always start in PENDING_REVIEW.
	 * An admin must approve the application before a PDF can be downloaded.
	 */
	public CustomerProduct createCustomerProduct(CustomerProductDto dto) {

		CustomerProduct customerProduct = new CustomerProduct();

		boolean isUpdate = dto.getId() > 0;

		if (isUpdate) {

			customerProduct = repo.findById(dto.getId());

			if (customerProduct == null) {
				throw ApiException.notFound(
						"Application " + dto.getId() + " was not found."
				);
			}
		}

		customerProduct =
				customerProduct == null
						? new CustomerProduct()
						: customerProduct;

		Customer customer =
				cService.getCustomer(dto.getCustomerId());

		Product product =
				pService.getProduct(dto.getProductId());

		if (!isUpdate) {

			boolean alreadyActive = !repo
					.findByCustomerAndProductAndStatusIn(
							customer,
							product,
							Arrays.asList(
									ApplicationStatus.PENDING_REVIEW,
									ApplicationStatus.APPROVED
							)
					)
					.isEmpty();

			if (alreadyActive) {
				throw ApiException.conflict(
						customer.getName()
								+ " already has a pending or approved application for "
								+ product.getName()
								+ "."
				);
			}
		}

		customerProduct.setId(dto.getId());
		customerProduct.setCustomer(customer);
		customerProduct.setProduct(product);

		customerProduct.setInGoodHealth(
				dto.isInGoodHealth()
		);

		customerProduct.setHealthStatus(
				dto.getHealthStatus()
		);

		customerProduct.setSpecificDiasgnosis(
				dto.isSpecificDiasgnosis()
		);

		customerProduct.setSpecificDiasgnosisStatus(
				dto.getSpecificDiasgnosisStatus()
		);

		customerProduct.setPaymentMade(
				dto.isPaymentMade()
		);

		if (!isUpdate) {

			Timestamp now =
					new Timestamp(System.currentTimeMillis());

			customerProduct.setCreatedOn(now);
			customerProduct.setCreatedBy(actor.current());
			customerProduct.setSubmittedOn(now);
			customerProduct.setStatus(
					ApplicationStatus.PENDING_REVIEW
			);

		} else {

			customerProduct.setEdittedOn(
					new Timestamp(System.currentTimeMillis())
			);

			customerProduct.setEdittedBy(
					actor.current()
			);
		}

		log.info("Customer product data saved...");

		customerProduct =
				repo.save(customerProduct);

		return customerProduct;
	}

	/**
	 * Admin accepts the application.
	 *
	 * Once approved, the PDF is pre-generated so that it is ready when
	 * the user requests it.
	 */
	public CustomerProduct approve(
			int id,
			String notes) {

		CustomerProduct customerProduct =
				getById(id);

		if (customerProduct.getStatus()
				== ApplicationStatus.APPROVED) {

			return customerProduct;
		}

		customerProduct.setStatus(
				ApplicationStatus.APPROVED
		);

		customerProduct.setReviewedOn(
				new Timestamp(System.currentTimeMillis())
		);

		customerProduct.setReviewedBy(
				actor.current()
		);

		customerProduct.setReviewNotes(notes);

		customerProduct =
				repo.save(customerProduct);

		/*
		 * Pre-generate the PDF after approval.
		 *
		 * A PDF-generation failure should not undo the approval itself.
		 * The customer can still request the PDF later, at which point
		 * it will be generated again.
		 */
		try {

			File generatedFile =
					applicationPDFGenerator
							.generateApplicationPDF(
									customerProduct
							);

			if (generatedFile == null) {
				log.warn(
						"Approved application {} but PDF pre-generation returned null",
						id
				);
			} else {
				log.info(
						"PDF pre-generated successfully for approved application {}",
						id
				);
			}

		} catch (Exception e) {

			log.warn(
					"Approved application {} but PDF pre-generation failed",
					id,
					e
			);
		}

		return customerProduct;
	}

	/**
	 * Admin declines the application.
	 *
	 * A reason is mandatory for the audit trail.
	 */
	public CustomerProduct reject(
			int id,
			String notes) {

		if (notes == null || notes.isBlank()) {

			throw ApiException.badRequest(
					"A reason is required to reject an application."
			);
		}

		CustomerProduct customerProduct =
				getById(id);

		customerProduct.setStatus(
				ApplicationStatus.REJECTED
		);

		customerProduct.setReviewedOn(
				new Timestamp(System.currentTimeMillis())
		);

		customerProduct.setReviewedBy(
				actor.current()
		);

		customerProduct.setReviewNotes(notes);

		return repo.save(customerProduct);
	}

	/**
	 * Generates the PDF for an approved application and returns the
	 * generated file bytes.
	 *
	 * Only APPROVED applications can download the final insurance document.
	 */
	public byte[] downloadApplicationPdf(int id)
			throws java.io.IOException {

		CustomerProduct customerProduct =
				getById(id);

		if (customerProduct.getStatus()
				!= ApplicationStatus.APPROVED) {

			throw ApiException.badRequest(
					"This application hasn't been approved yet, "
							+ "so no document is available."
			);
		}

		File file =
				applicationPDFGenerator
						.generateApplicationPDF(
								customerProduct
						);

		if (file == null || !file.exists()) {

			throw ApiException.badRequest(
					"Couldn't generate the document. "
							+ "Check the customer has a date of birth on file."
			);
		}

		if (file.length() == 0) {

			throw ApiException.badRequest(
					"The generated document is empty."
			);
		}

		log.info(
				"Reading generated PDF for application {} from {} ({} bytes)",
				id,
				file.getAbsolutePath(),
				file.length()
		);

		return Files.readAllBytes(
				file.toPath()
		);
	}

	/**
	 * Legacy email-based path kept for the existing customer application.
	 *
	 * Finds the customer's latest approved application and downloads its PDF.
	 */
	public byte[] downloadApplicationPdf(String email)
			throws java.io.IOException {

		Customer customer =
				cService.getCustomerByEmail(email);

		List<CustomerProduct> approved =
				repo.findByCustomerAndStatusOrderByReviewedOnDesc(
						customer,
						ApplicationStatus.APPROVED
				);

		if (approved.isEmpty()) {

			throw ApiException.badRequest(
					"This customer has no approved application yet."
			);
		}

		return downloadApplicationPdf(
				approved.get(0).getId()
		);
	}

	public CustomerProduct getById(int id) {

		CustomerProduct customerProduct =
				repo.findById(id);

		if (customerProduct == null) {

			throw ApiException.notFound(
					"Application " + id + " was not found."
			);
		}

		return customerProduct;
	}

	public CustomerProduct getCustomerProduct(
			int customerId,
			int productId) {

		return repo.findByCustomerAndProduct(
				cService.getCustomer(customerId),
				pService.getProduct(productId)
		);
	}

	public List<CustomerProduct> getCustomerProducts(
			String email) {

		return repo.findByCustomerOrderByCreatedOnDesc(
				cService.getCustomerByEmail(email)
		);
	}

	/**
	 * Audit list: every application in the system, newest first.
	 */
	public List<CustomerProduct> findAll() {

		return repo.findAllByOrderByCreatedOnDesc();
	}

	public List<CustomerProduct> findByStatus(
			ApplicationStatus status) {

		return repo.findByStatusOrderByCreatedOnDesc(
				status
		);
	}

	public DashboardStatsDto getDashboardStats() {

		long total =
				repo.count();

		long pending =
				repo.countByStatus(
						ApplicationStatus.PENDING_REVIEW
				);

		long approved =
				repo.countByStatus(
						ApplicationStatus.APPROVED
				);

		long rejected =
				repo.countByStatus(
						ApplicationStatus.REJECTED
				);

		return new DashboardStatsDto(
				cService.getCustomers().size(),
				pService.getProducts().size(),
				total,
				pending,
				approved,
				rejected
		);
	}
}