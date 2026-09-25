package com.abcbank.insurance.dto;


import java.sql.Date;

import lombok.Data;

@Data
public class DependantDto {
	private int id;
	private String name;
	private Date dateOfBirth;
	private String idNumber;
	private String relationship;
	private String mobileNumber;
	private String email;
	private int customerId;
	private String type;
}