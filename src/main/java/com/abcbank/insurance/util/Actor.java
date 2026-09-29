package com.abcbank.insurance.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves "who is doing this" for audit columns (createdBy / reviewedBy ...).
 *
 * Authentication comes later (Keycloak/OAuth). Until then the UI sends an
 * X-User header and we fall back to "admin". When auth lands, only this class
 * needs to change to read the authenticated principal instead.
 */
@Component
public class Actor {

	public String current() {
		String user = null;
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attrs != null) {
				user = attrs.getRequest().getHeader("X-User");
			}
		} catch (Exception ignored) {
			// no request bound (e.g. called from a runner) -> default below
		}
		if (user == null || user.isBlank()) {
			user = "admin";
		}
		user = user.trim();
		return user.length() > 32 ? user.substring(0, 32) : user;
	}
}
