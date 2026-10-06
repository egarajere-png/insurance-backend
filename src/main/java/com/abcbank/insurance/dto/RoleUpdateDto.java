package com.abcbank.insurance.dto;

import lombok.Data;

@Data
public class RoleUpdateDto {
	/** "ADMIN" or "CUSTOMER" — SUPERADMIN can't be granted through this endpoint. */
	private String role;
}
