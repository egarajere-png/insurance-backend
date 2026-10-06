package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.repo.AppUserRepo;
import com.abcbank.insurance.util.CurrentUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AppUserService {

	@Autowired
	private AppUserRepo repo;
	@Autowired
	private CurrentUser currentUser;

	/**
	 * Email that gets SUPERADMIN automatically on their very first sign-in —
	 * how the very first superadmin gets into a brand-new deployment, since
	 * there's no one yet to promote them. Set via app.superadmin-email.
	 * Leave unset in production once at least one superadmin exists.
	 */
	@Value("${app.superadmin-email:}")
	private String bootstrapSuperadminEmail;

	/** Called by FirebaseAuthFilter on every authenticated request. */
	public AppUser resolveOrProvision(String firebaseUid, String email) {
		AppUser user = repo.findByFirebaseUid(firebaseUid);
		if (user == null) {
			user = new AppUser();
			user.setFirebaseUid(firebaseUid);
			user.setEmail(email);
			boolean isBootstrapSuperadmin = !bootstrapSuperadminEmail.isBlank()
					&& bootstrapSuperadminEmail.trim().equalsIgnoreCase(email);
			user.setRole(isBootstrapSuperadmin ? Role.SUPERADMIN : Role.CUSTOMER);
			user.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			if (isBootstrapSuperadmin) {
				log.info("Provisioning {} as the bootstrap SUPERADMIN.", email);
			}
		}
		user.setEmail(email); // keep in sync in case it changed on the Firebase side
		user.setLastLoginOn(new Timestamp(System.currentTimeMillis()));
		return repo.save(user);
	}

	/** Links a freshly-created Customer record to the signed-in user (the "complete your profile" step). */
	public AppUser linkCustomer(int appUserId, int customerId) {
		AppUser user = repo.findById(appUserId).orElse(null);
		if (user == null) {
			throw ApiException.notFound("User not found.");
		}
		user.setCustomerId(customerId);
		return repo.save(user);
	}

	/** Superadmin-only: every signed-in user, for the role-management screen. */
	public List<AppUser> listAll() {
		currentUser.require(Role.SUPERADMIN);
		return repo.findAllByOrderByCreatedOnDesc();
	}

	/** Superadmin-only: promote/demote between ADMIN and CUSTOMER. Can't touch SUPERADMIN accounts (including your own). */
	public AppUser updateRole(int id, Role newRole) {
		AppUser actingUser = currentUser.require(Role.SUPERADMIN);
		if (newRole == Role.SUPERADMIN) {
			throw ApiException.badRequest("Superadmin status can't be granted from this screen.");
		}
		AppUser target = repo.findById(id).orElse(null);
		if (target == null) {
			throw ApiException.notFound("User not found.");
		}
		if (target.getRole() == Role.SUPERADMIN) {
			throw ApiException.badRequest("Superadmin accounts can't be changed here.");
		}
		if (target.getId() == actingUser.getId()) {
			throw ApiException.badRequest("You can't change your own role.");
		}
		target.setRole(newRole);
		return repo.save(target);
	}
}
