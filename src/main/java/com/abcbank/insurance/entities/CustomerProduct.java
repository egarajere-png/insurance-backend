package com.abcbank.insurance.entities;

import java.sql.Timestamp;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
	@JoinColumn(nullable=true, columnDefinition = "boolean default false")
	private boolean paymentMade = false;
	@JoinColumn(nullable=true, columnDefinition = "boolean default true")
	private boolean inGoodHealth = true;
	@Column(length = 512, nullable=true)
	private String healthStatus;
	@JoinColumn(nullable=true, columnDefinition = "boolean default false")
	private boolean specificDiasgnosis = true;
	@Column(length = 512, nullable=true)
	private String specificDiasgnosisStatus;
	private Timestamp createdOn;
	@Column(length = 32)
	private String createdBy;
	private Timestamp edittedOn;
	@Column(length = 32)
	private String edittedBy;
	@ManyToOne
    @JoinColumn(name="customer_id", nullable=false)
    private Customer customer;
	@ManyToOne
    @JoinColumn(name="product_id", nullable=false)
    private Product product;
}