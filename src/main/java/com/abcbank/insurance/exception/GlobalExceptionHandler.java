package com.abcbank.insurance.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

/**
 * Turns every failure into a clean JSON body: { status, error, message, timestamp }.
 * The UI reads "message" directly, so users see a readable reason instead of a bare 500.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
		return build(ex.getStatus(), ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Validation failed" : message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body (check dates are yyyy-MM-dd and numbers are valid).");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return build(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
		String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
		log.warn("Data integrity violation: {}", root);
		String lower = root == null ? "" : root.toLowerCase();
		String message;
		if (lower.contains("too long")) {
			message = "One of the values is too long for its field.";
		} else if (lower.contains("null value") || lower.contains("not-null")) {
			message = "A required field is missing.";
		} else if (lower.contains("foreign key")) {
			message = "This record is referenced by other records and cannot be changed or removed.";
		} else if (lower.contains("duplicate") || lower.contains("unique")) {
			message = "A record with these details already exists.";
		} else {
			message = "The data could not be saved because it violates a database rule.";
		}
		return build(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
		log.error("Unhandled error", ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error. Please try again or contact support.");
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
	}
}
