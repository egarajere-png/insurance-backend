package com.abcbank.insurance.services;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.CustomerProductDto;
import com.abcbank.insurance.dto.DashboardStatsDto;
import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.ApplicationStatus;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Dependant;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.pdf.ApplicationPDFGenerator;
import com.abcbank.insurance.repo.CustomerProductRepo;
import com.abcbank.insurance.util.Actor;
import com.abcbank.insurance.util.CurrentUser;
import com.abcbank.insurance.util.Text;

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
	private DependantService dService;

	@Autowired
	private ApplicationPDFGenerator applicationPDFGenerator;

	@Autowired
	private Actor actor;

	@Autowired
	private CurrentUser currentUser;

	/**
	 * Customer finishes/submits their application.
	 *
	 * New applications always enter PENDING_REVIEW.
	 * An admin must approve the application before a PDF can be produced.
	 *
	 * A customer may not re-apply for the same product while an application
	 * for that product is pending or already approved.
	 */
	public CustomerProduct createCustomerProduct(CustomerProductDto dto) {

		CustomerProduct customerProduct = new CustomerProduct();

		boolean isUpdate = dto.getId() > 0;

		boolean isCustomerRole = currentUser.get().getRole() == Role.CUSTOMER;

		if (isUpdate) {
			customerProduct = repo.findById(dto.getId());

			if (customerProduct == null) {
				throw ApiException.notFound(
						"Application " + dto.getId() + " was not found."
				);
			}

			/*
			 * Ownership is checked against the STORED application's owner, never
			 * against the customerId in the request body (which the caller
			 * controls). Otherwise a customer could edit someone else's
			 * application by sending their own customerId with another
			 * application's id.
			 */
			requireOwnCustomerIfCustomerRole(
					customerProduct.getCustomer().getId()
			);

			/*
			 * An application can't be handed to another customer or switched to
			 * another product by an update (the switch would also skip the
			 * duplicate-application check below).
			 */
			if (customerProduct.getCustomer().getId() != dto.getCustomerId()) {
				throw ApiException.badRequest(
						"An application can't be moved to a different customer."
				);
			}
			if (customerProduct.getProduct().getId() != dto.getProductId()) {
				throw ApiException.badRequest(
						"The product on an existing application can't be changed. Submit a new application instead."
				);
			}

			/*
			 * Once an admin has decided, the application is part of the audit
			 * trail and the approved PDF must match what was reviewed, so only
			 * applications still waiting for review may be edited.
			 */
			if (customerProduct.getStatus() != ApplicationStatus.PENDING_REVIEW) {
				throw ApiException.conflict(
						"Only applications that are pending review can be edited."
				);
			}
		}

		Customer customer = cService.getCustomer(dto.getCustomerId());
		Product product = pService.getProduct(dto.getProductId());

		/*
		 * A CUSTOMER may only create/update an application belonging to
		 * their own customer profile.
		 */
		requireOwnCustomerIfCustomerRole(customer.getId());

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

		/*
		 * A negative health answer requires an explanation.
		 */
		if (!dto.isInGoodHealth()
				&& Text.orNull(dto.getHealthStatus()) == null) {

			throw ApiException.badRequest(
					"Please describe the health status since the applicant is not in good health."
			);
		}

		/*
		 * A specific diagnosis requires details.
		 */
		if (dto.isSpecificDiasgnosis()
				&& Text.orNull(dto.getSpecificDiasgnosisStatus()) == null) {

			throw ApiException.badRequest(
					"Please provide diagnosis details since a specific diagnosis was declared."
			);
		}

		customerProduct.setId(dto.getId());
		customerProduct.setCustomer(customer);
		customerProduct.setProduct(product);
		customerProduct.setInGoodHealth(dto.isInGoodHealth());
		customerProduct.setHealthStatus(dto.getHealthStatus());
		customerProduct.setSpecificDiasgnosis(dto.isSpecificDiasgnosis());
		customerProduct.setSpecificDiasgnosisStatus(
				dto.getSpecificDiasgnosisStatus()
		);
		/*
		 * Only staff may record a payment; a customer's request can never
		 * mark their own application as paid (new applications default to
		 * unpaid, and an existing value is left untouched).
		 */
		if (!isCustomerRole) {
			customerProduct.setPaymentMade(dto.isPaymentMade());
		}

		customerProduct.setCoveredPeople(
				coveredPeopleFor(customer, dto.getCoveredPersonIds())
		);

		if (!isUpdate) {

			Timestamp now = new Timestamp(
					System.currentTimeMillis()
			);

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

		customerProduct = repo.save(customerProduct);

		return customerProduct;
	}

	/**
	 * A CUSTOMER-role user may only act on their own linked customer record.
	 */
	private void requireOwnCustomerIfCustomerRole(int customerId) {

		AppUser user = currentUser.get();

		if (user.getRole() == Role.CUSTOMER
				&& (
						user.getCustomerId() == null
						|| user.getCustomerId() != customerId
				)) {

			throw new ApiException(
					HttpStatus.FORBIDDEN,
					"You can only manage your own applications."
			);
		}
	}

	/**
	 * TEMPORARY: choosing which dependants/beneficiaries an application covers
	 * is switched off while the bank owners decide how it should work. While
	 * this is false, every application covers ALL of the customer's
	 * dependants and beneficiaries and any coveredPersonIds sent by a client
	 * are ignored. Flip to true (and restore the selection step in the UI) to
	 * bring per-application selection back.
	 */
	private static final boolean ALLOW_COVERAGE_SELECTION = false;

	private Set<Dependant> coveredPeopleFor(Customer customer, List<Integer> requestedIds) {
		if (ALLOW_COVERAGE_SELECTION) {
			return resolveCoveredPeople(customer, requestedIds);
		}
		return new LinkedHashSet<>(dService.getCustomerDependants(customer));
	}

	/**
	 * Resolves the selected dependant/beneficiary IDs into entities.
	 *
	 * Each selected person must belong to the same customer who owns
	 * the application.
	 */
	private Set<Dependant> resolveCoveredPeople(
			Customer customer,
			List<Integer> ids
	) {

		Set<Dependant> people = new LinkedHashSet<>();

		if (ids == null) {
			return people;
		}

		for (Integer id : ids) {

			if (id == null) {
				continue;
			}

			Dependant dependant = dService.getDependant(id);

			if (
					dependant.getCustomer() == null
					|| dependant.getCustomer().getId() != customer.getId()
			) {

				throw ApiException.badRequest(
						"One of the selected people doesn't belong to this customer."
				);
			}

			people.add(dependant);
		}

		return people;
	}

	/**
	 * Admin accepts an application.
	 *
	 * Once approved, the customer becomes eligible to download
	 * the final insurance PDF.
	 */
	public CustomerProduct approve(int id, String notes) {

		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

		CustomerProduct customerProduct = getById(id);

		if (
				customerProduct.getStatus()
						== ApplicationStatus.APPROVED
		) {
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

		/*
		 * Everyone on the customer's file is covered; pick up anyone added
		 * after the application was submitted.
		 */
		customerProduct.setCoveredPeople(
				coveredPeopleFor(customerProduct.getCustomer(), null)
		);

		customerProduct = repo.save(customerProduct);

		/*
		 * Pre-generate the PDF so it is ready immediately after approval.
		 */
		try {

			applicationPDFGenerator.generateApplicationPDF(
					customerProduct
			);

		} catch (Exception e) {

			log.warn(
					"Approved application {} but PDF pre-generation failed: {}",
					id,
					e.getMessage()
			);
		}

		return customerProduct;
	}

	/**
	 * Admin rejects an application.
	 *
	 * A rejection reason is mandatory and becomes visible to
	 * the customer.
	 */
	public CustomerProduct reject(int id, String notes) {

		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

		if (notes == null || notes.isBlank()) {

			throw ApiException.badRequest(
					"A reason is required to reject an application."
			);
		}

		CustomerProduct customerProduct = getById(id);

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
	 * Generates or regenerates the PDF for an approved application.
	 *
	 * Only APPROVED applications can be downloaded.
	 */
	public byte[] downloadApplicationPdf(int id)
			throws java.io.IOException {

		CustomerProduct customerProduct = getById(id);

		if (
				customerProduct.getStatus()
						!= ApplicationStatus.APPROVED
		) {

			throw ApiException.badRequest(
					"This application hasn't been approved yet, so no document is available."
			);
		}

		File file =
				applicationPDFGenerator.generateApplicationPDF(
						customerProduct
				);

		if (
				file == null
				|| !file.exists()
		) {

			throw ApiException.badRequest(
					"Couldn't generate the document. Check the customer has a date of birth on file."
			);
		}

		return Files.readAllBytes(
				file.toPath()
		);
	}

	/**
	 * Legacy email-based PDF lookup.
	 *
	 * The final getById() call still performs the ownership check for
	 * customer users.
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

	/**
	 * Gets one application.
	 *
	 * CUSTOMER:
	 *     Can only access their own application.
	 *
	 * ADMIN/SUPERADMIN:
	 *     Can access any application.
	 */
	public CustomerProduct getById(int id) {

		CustomerProduct customerProduct =
				repo.findById(id);

		if (customerProduct == null) {

			throw ApiException.notFound(
					"Application " + id + " was not found."
			);
		}

		requireOwnCustomerIfCustomerRole(
				customerProduct.getCustomer().getId()
		);

		return customerProduct;
	}

	public CustomerProduct getCustomerProduct(
			int customerId,
			int productId
	) {

		return repo.findByCustomerAndProduct(
				cService.getCustomer(customerId),
				pService.getProduct(productId)
		);
	}

	/**
	 * Customer application list.
	 *
	 * IMPORTANT:
	 * For CUSTOMER users, the email supplied by the frontend is NOT trusted.
	 * The backend uses the authenticated Firebase/AppUser customerId instead.
	 *
	 * This prevents:
	 *
	 * /customer-product/list/someone-else@gmail.com
	 *
	 * from exposing another customer's applications.
	 */
	public List<CustomerProduct> getCustomerProducts(
			String email
	) {

		AppUser user = currentUser.get();

		if (user.getRole() == Role.CUSTOMER) {

			if (user.getCustomerId() == null) {

				throw new ApiException(
						HttpStatus.FORBIDDEN,
						"Your account is not linked to a customer profile."
				);
			}

			Customer ownCustomer =
					cService.getCustomer(
							user.getCustomerId()
					);

			return repo.findByCustomerOrderByCreatedOnDesc(
					ownCustomer
			);
		}

		/*
		 * Admins can still use the email-based lookup for backward
		 * compatibility, although the main admin application page
		 * uses /customer-product/list.
		 */
		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

		return repo.findByCustomerOrderByCreatedOnDesc(
				cService.getCustomerByEmail(email)
		);
	}

	/**
	 * Audit list:
	 * Every application in the system, newest first.
	 *
	 * ADMIN/SUPERADMIN only.
	 */
	public List<CustomerProduct> findAll() {

		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

		return repo.findAllByOrderByCreatedOnDesc();
	}

	/**
	 * Admin-only application list filtered by status.
	 */
	public List<CustomerProduct> findByStatus(
			ApplicationStatus status
	) {

		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

		return repo.findByStatusOrderByCreatedOnDesc(
				status
		);
	}

	/**
	 * Global dashboard statistics.
	 *
	 * These numbers represent the entire insurance system,
	 * therefore they are only exposed to admins.
	 */
	public DashboardStatsDto getDashboardStats() {

		currentUser.require(
				Role.ADMIN,
				Role.SUPERADMIN
		);

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