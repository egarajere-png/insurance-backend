package com.abcbank.insurance.entities;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table
public class CustomerProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(nullable = true)
	private boolean paymentMade = false;
	@Column(nullable = true)
	private boolean inGoodHealth = true;
	@Column(length = 512, nullable = true)
	private String healthStatus;
	@Column(nullable = true)
	private boolean specificDiasgnosis = false;
	@Column(length = 512, nullable = true)
	private String specificDiasgnosisStatus;

	// ---- Review workflow / audit trail ----
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ApplicationStatus status = ApplicationStatus.PENDING_REVIEW;
	/** When the customer finished the application (it entered review). */
	private Timestamp submittedOn;
	/** When an admin approved or rejected it. */
	private Timestamp reviewedOn;
	@Column(length = 64)
	private String reviewedBy;
	@Column(length = 512)
	private String reviewNotes;

	// createdBy = who initiated the application
	private Timestamp createdOn;
	@Column(length = 32)
	private String createdBy;
	private Timestamp edittedOn;
	@Column(length = 32)
	private String edittedBy;
	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/**
	 * Which of the customer's dependants/beneficiaries this specific
	 * application covers — chosen at application time (all, some, or none),
	 * not implicitly "every dependant the customer has". Drives the PDF's
	 * dependants section.
	 */
	@ManyToMany
	@JoinTable(name = "application_covered_people", joinColumns = @JoinColumn(name = "application_id"), inverseJoinColumns = @JoinColumn(name = "dependant_id"))
	private Set<Dependant> coveredPeople = new LinkedHashSet<>();
}
