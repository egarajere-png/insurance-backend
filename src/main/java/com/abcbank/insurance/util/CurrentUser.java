package com.abcbank.insurance.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * Reads the AppUser that FirebaseAuthFilter resolved and attached to this
 * request. Mirrors the style of {@link Actor} (RequestContextHolder lookup)
 * so controllers don't need HttpServletRequest injected everywhere.
 */
@Component
public class CurrentUser {

	public static final String REQUEST_ATTRIBUTE = "currentAppUser";

	public AppUser get() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
		}
		HttpServletRequest request = attrs.getRequest();
		AppUser user = (AppUser) request.getAttribute(REQUEST_ATTRIBUTE);
		if (user == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
		}
		return user;
	}

	/** Throws 403 unless the current user has one of the given roles. */
	public AppUser require(Role... allowed) {
		AppUser user = get();
		for (Role role : allowed) {
			if (user.getRole() == role) {
				return user;
			}
		}
		throw new ApiException(HttpStatus.FORBIDDEN, "You don't have permission to do that.");
	}
}
