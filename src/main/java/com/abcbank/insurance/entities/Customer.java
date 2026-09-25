package com.abcbank.insurance.entities;


import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(length = 100, nullable = false)
	private String name;
	private Date dateOfBirth;
	@Column(length = 32, nullable = false)
	private String idNumber;
	@Column(length = 32, nullable = false, columnDefinition = "varchar(32) default ''")
	private String pinNumber;
	@Column(length = 64, nullable = false, columnDefinition = "varchar(64) default ''")
	private String occupation;
	@Column(length = 1, nullable = false)
	private String gender;
	@Column(length = 20, nullable = false)
	private String mobileNumber;
	@Column(length = 100, nullable = false)
	private String emailAddress;
	@Column(length = 100, nullable = false)
	private String postalAddress;
	@Column(length = 32, nullable = false, columnDefinition = "varchar(32) default ''")
	private String postalCode;
	@Column(length = 64, nullable = false, columnDefinition = "varchar(64) default ''")
	private String city;
	@Column(nullable = false)
	private int dependantsNo;
	private Timestamp createdOn;
	@Column(length = 32)
	private String createdBy;
	private Timestamp edittedOn;
	@Column(length = 32)
	private String edittedBy;
	@Transient
	private List<Dependant> dependants;
	@Transient
	private Dependant nominatedBeneficiary;
}