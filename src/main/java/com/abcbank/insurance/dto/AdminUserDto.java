package com.abcbank.insurance.dto;

import java.sql.Timestamp;

import com.abcbank.insurance.entities.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the superadmin Users screen: the sign-in identity plus a few
 * fields from the linked Customer profile (when there is one), so the screen
 * can search by name, phone number or national ID as well as email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
	private int id;
	private String email;
	private Role role;
	private Integer customerId;
	private Timestamp createdOn;
	private Timestamp lastLoginOn;
	/** Null until the user has completed their profile (and always null for admins without one). */
	private String customerName;
	private String customerMobile;
	private String customerIdNumber;
}
