package com.abcbank.insurance.dto;


import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerDto {
	private int id;
	@NotBlank(message = "Name is required")
	@Size(max = 100, message = "Name is too long (max 100)")
	private String name;
	private Date dateOfBirth;
	@NotBlank(message = "ID number is required")
	@Size(max = 32, message = "ID number is too long (max 32)")
	private String idNumber;
	@Size(max = 32, message = "PIN number is too long (max 32)")
	private String pinNumber;
	@Size(max = 64, message = "Occupation is too long (max 64)")
	private String occupation;
	/** Male / Female / Other (M and F are also accepted). Optional. */
	@Size(max = 16, message = "Gender is too long")
	private String gender;
	@NotBlank(message = "Mobile number is required")
	@Size(max = 20, message = "Mobile number is too long (max 20)")
	private String mobileNumber;
	@NotBlank(message = "Email address is required")
	@Email(message = "Email address is not valid")
	@Size(max = 100, message = "Email address is too long (max 100)")
	private String emailAddress;
	@Size(max = 100, message = "Postal address is too long (max 100)")
	private String postalAddress;
	@Size(max = 32, message = "Postal code is too long (max 32)")
	private String postalCode;
	@Size(max = 64, message = "City is too long (max 64)")
	private String city;
}
