package com.abcbank.insurance.dto;


import java.sql.Date;

import lombok.Data;

@Data
public class CustomerDto {
	private int id;
	private String name;
	private Date dateOfBirth;
	private String idNumber;
	private String pinNumber;
	private String occupation;
	private String gender;
	private String mobileNumber;
	private String emailAddress;
	private String postalAddress;
	private String postalCode;
	private String city;
}