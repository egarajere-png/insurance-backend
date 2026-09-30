package com.abcbank.insurance.entities;


import java.util.Date;
import java.sql.Timestamp;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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
public class Dependant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(length = 100, nullable = false)
	private String name;
	private Date dateOfBirth;
	@Column(length = 32, nullable = false)
	private String idNumber;
	@Column(length = 32, nullable = false)
	private String mobileNumber;
	@Column(length = 32, nullable = false)
	private String relationship;
	@Column(length = 64, nullable = true, columnDefinition = "varchar(64) default ''")
	private String email;
	private PersonType personType;

	private Timestamp createdOn;
	@Column(length = 32)
	private String createdBy;
	private Timestamp edittedOn;
	@Column(length = 32)
	private String edittedBy;
	@ManyToOne
    @JoinColumn(name="customer_id", nullable=false)
    private Customer customer;
}