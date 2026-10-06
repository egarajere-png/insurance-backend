package com.abcbank.insurance.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.AuthMeDto;
import com.abcbank.insurance.dto.CustomerDto;
import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.services.AppUserService;
import com.abcbank.insurance.services.CustomerService;
import com.abcbank.insurance.util.CurrentUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/insurance/auth")
public class AuthController {

	@Autowired
	private CurrentUser currentUser;
	@Autowired
	private CustomerService cService;
	@Autowired
	private AppUserService appUserService;

	/** Called right after sign-in so the frontend knows: role, and whether profile-completion is still needed. */
	@GetMapping("/me")
	public AuthMeDto me() {
		AppUser user = currentUser.get();
		Customer customer = user.getCustomerId() == null ? null : cService.getCustomer(user.getCustomerId());
		return new AuthMeDto(user.getId(), user.getEmail(), user.getRole(), user.getCustomerId(), customer);
	}

	/**
	 * First-time customer flow: creates the Customer record from the profile
	 * form and links it to the signed-in Firebase identity in one step, so a
	 * customer can't end up signed in with no linked profile (or vice versa).
	 */
	@PostMapping("/complete-profile")
	public AuthMeDto completeProfile(@Valid @RequestBody CustomerDto dto) {
		AppUser user = currentUser.get();
		if (user.getCustomerId() != null) {
			throw ApiException.badRequest("Your profile is already set up.");
		}
		dto.setId(0);
		dto.setEmailAddress(user.getEmail()); // the profile belongs to the signed-in identity, not whatever was typed
		Customer customer = cService.createCustomer(dto);
		user = appUserService.linkCustomer(user.getId(), customer.getId());
		return new AuthMeDto(user.getId(), user.getEmail(), user.getRole(), user.getCustomerId(), customer);
	}
}
