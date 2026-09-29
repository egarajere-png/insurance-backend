package com.abcbank.insurance.dto;

import lombok.Data;

/** Body for approve / reject. "notes" is the admin's comment; required when rejecting. */
@Data
public class ReviewDto {
	private String notes;
}
