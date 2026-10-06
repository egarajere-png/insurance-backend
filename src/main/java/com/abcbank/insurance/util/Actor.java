package com.abcbank.insurance.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.abcbank.insurance.entities.AppUser;

/**
 * Resolves "who is doing this" for audit columns (createdBy / reviewedBy ...).
 *
 * Now that requests are authenticated (FirebaseAuthFilter), this is the
 * verified signed-in user's email — not a client-supplied, spoofable header.
 * The old X-User header / "admin" default are kept as a fallback only for
 * calls made outside the filter (e.g. a background job with no request bound).
 */
@Component
public class Actor {

	@Autowired
	private CurrentUser currentUser;

	public String current() {
		try {
			AppUser user = currentUser.get();
			return clip(user.getEmail());
		} catch (Exception ignored) {
			// not an authenticated request (shouldn't normally happen under
			// FirebaseAuthFilter) -> fall back to the legacy header/default
		}
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
		return clip(user);
	}

	private String clip(String user) {
		user = user.trim();
		return user.length() > 32 ? user.substring(0, 32) : user;
	}
}
