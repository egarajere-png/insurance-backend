package com.abcbank.insurance.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.abcbank.insurance.config.FirebaseConfig;
import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.services.AppUserService;
import com.abcbank.insurance.util.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies the Firebase ID token on every /api/** request (except the CORS
 * preflight OPTIONS method, which never carries one) and attaches the
 * resolved AppUser to the request for {@link CurrentUser} / controllers to
 * read. No token (or an invalid one) short-circuits with 401 before the
 * request reaches a controller.
 */
@Slf4j
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

	@Autowired
	private AppUserService appUserService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "OPTIONS".equalsIgnoreCase(request.getMethod()) || !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!FirebaseConfig.initialized) {
			writeError(response, HttpStatus.SERVICE_UNAVAILABLE, "Authentication isn't configured on the server yet.");
			return;
		}

		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			writeError(response, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header.");
			return;
		}
		String idToken = header.substring("Bearer ".length()).trim();

		try {
			FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
			AppUser user = appUserService.resolveOrProvision(decoded.getUid(), decoded.getEmail());
			request.setAttribute(CurrentUser.REQUEST_ATTRIBUTE, user);
			chain.doFilter(request, response);
		} catch (Exception e) {
			log.warn("Rejected request with invalid Firebase token: {}", e.getMessage());
			writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired session. Please sign in again.");
		}
	}

	/** Same JSON shape as GlobalExceptionHandler, since this runs before Spring MVC/@RestControllerAdvice ever sees the request. */
	private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
