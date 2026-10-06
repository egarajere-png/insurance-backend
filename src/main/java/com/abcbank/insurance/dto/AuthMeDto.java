package com.abcbank.insurance.dto;

import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** What the frontend needs right after sign-in to decide where to route the user. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthMeDto {
	private int id;
	private String email;
	private Role role;
	private Integer customerId;
	/** Populated only once customerId is set — saves the frontend a second round-trip. */
	private Customer customer;
}
