package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.RoleUpdateDto;
import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.services.AppUserService;

/** Superadmin-only: list signed-in users and promote/demote between ADMIN and CUSTOMER. */
@RestController
@RequestMapping("/api/insurance/admin/users")
public class AdminUserController {

	@Autowired
	private AppUserService appUserService;

	@GetMapping
	public List<AppUser> list() {
		return appUserService.listAll();
	}

	@PostMapping("/{id}/role")
	public AppUser updateRole(@PathVariable int id, @RequestBody RoleUpdateDto dto) {
		Role role = parseRole(dto.getRole());
		return appUserService.updateRole(id, role);
	}

	private Role parseRole(String raw) {
		if (raw == null) {
			throw ApiException.badRequest("Role is required.");
		}
		try {
			return Role.valueOf(raw.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw ApiException.badRequest("Role must be ADMIN or CUSTOMER.");
		}
	}
}
