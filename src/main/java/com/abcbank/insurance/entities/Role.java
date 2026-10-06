package com.abcbank.insurance.entities;

/**
 * SUPERADMIN can do everything ADMIN can, plus promote/demote other users'
 * roles — that is the only extra power it has. CUSTOMER is the restricted,
 * self-service role (their own portal, own data only).
 */
public enum Role {
	SUPERADMIN, ADMIN, CUSTOMER;
}
