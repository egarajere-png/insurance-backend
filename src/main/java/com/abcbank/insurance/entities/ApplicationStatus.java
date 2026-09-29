package com.abcbank.insurance.entities;

/**
 * Lifecycle of an insurance application (CustomerProduct).
 *
 * PENDING_REVIEW : customer has finished the application; waiting for an admin.
 * APPROVED       : admin accepted it; the insurance PDF may now be downloaded.
 * REJECTED       : admin declined it (reason recorded for audit).
 */
public enum ApplicationStatus {
	PENDING_REVIEW, APPROVED, REJECTED;
}
