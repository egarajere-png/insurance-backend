package com.abcbank.insurance.entities;

import java.sql.Timestamp;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private Plan plan;
	@Column(length = 100, nullable = false)
	private String name;
	@Column(length = 1024, nullable = false)
	private String description;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private int benefit = 0;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private int benefitChild = 0;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private int premium = 0;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private int premiumAdditionalChild = 0;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private int premiumAdditionalAdultChild = 0;
	private Timestamp createdOn;
	@Column(length = 32)
	private String createdBy;
	private Timestamp edittedOn;
	@Column(length = 32)
	private String edittedBy;
}