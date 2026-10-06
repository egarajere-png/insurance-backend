package com.abcbank.insurance.entities;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * One row per signed-in Firebase identity. Role lives here (in our own DB),
 * not as a Firebase custom claim — a promotion/demotion then takes effect on
 * the user's very next request instead of waiting for them to get a fresh
 * ID token, which is what custom claims would require.
 */
@Data
@Entity
@Table(name = "app_user", uniqueConstraints = { @jakarta.persistence.UniqueConstraint(columnNames = "firebaseUid") })
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(nullable = false, length = 128)
	private String firebaseUid;
	@Column(nullable = false, length = 100)
	private String email;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role = Role.CUSTOMER;
	/**
	 * Links to the Customer record once this user has completed their profile
	 * (CUSTOMER role only — null until then, and for ADMIN/SUPERADMIN).
	 */
	private Integer customerId;
	private Timestamp createdOn;
	private Timestamp lastLoginOn;
}
