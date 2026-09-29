package com.abcbank.insurance.util;

import com.abcbank.insurance.exception.ApiException;

public final class Text {
	private Text() {
	}

	/** Trims; null becomes "" (for NOT NULL varchar columns that default to ''). */
	public static String orEmpty(String s) {
		return s == null ? "" : s.trim();
	}

	/** Trims; blank becomes null. */
	public static String orNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}

	public static String required(String value, String label) {
		if (value == null || value.isBlank()) {
			throw ApiException.badRequest(label + " is required.");
		}
		return value.trim();
	}

	public static String clip(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() > max ? s.substring(0, max) : s;
	}

	/**
	 * Accepts M / F / Male / Female / Other (any case). Stores "Male", "Female"
	 * or "Other". Blank -> null. Anything else -> 400 (instead of a DB 500).
	 */
	public static String normalizeGender(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		switch (raw.trim().toLowerCase()) {
		case "m":
		case "male":
			return "Male";
		case "f":
		case "female":
			return "Female";
		case "o":
		case "other":
			return "Other";
		default:
			throw ApiException.badRequest("Gender must be Male, Female or Other.");
		}
	}
}
