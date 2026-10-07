package com.abcbank.insurance.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.dto.AdminUserDto;
import com.abcbank.insurance.entities.AppUser;
import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.exception.ApiException;
import com.abcbank.insurance.repo.AppUserRepo;
import com.abcbank.insurance.repo.CustomerRepo;
import com.abcbank.insurance.util.CurrentUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AppUserService {

	@Autowired
	private AppUserRepo repo;
	@Autowired
	private CustomerRepo customerRepo;
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

	/** Superadmin-only: every signed-in user (with their customer profile's name/phone/ID when linked), for the role-management screen. */
	public List<AdminUserDto> listAll() {
		currentUser.require(Role.SUPERADMIN);
		List<AppUser> users = repo.findAllByOrderByCreatedOnDesc();
		List<Integer> customerIds = users.stream().map(AppUser::getCustomerId).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());
		Map<Integer, Customer> customersById = customerRepo.findAllById(customerIds).stream()
				.collect(Collectors.toMap(Customer::getId, Function.identity()));
		return users.stream()
				.map(u -> toAdminDto(u, u.getCustomerId() == null ? null : customersById.get(u.getCustomerId())))
				.collect(Collectors.toList());
	}

	private AdminUserDto toAdminDto(AppUser user, Customer customer) {
		return new AdminUserDto(user.getId(), user.getEmail(), user.getRole(), user.getCustomerId(), user.getCreatedOn(),
				user.getLastLoginOn(), customer == null ? null : customer.getName(),
				customer == null ? null : customer.getMobileNumber(), customer == null ? null : customer.getIdNumber());
	}

	/** Superadmin-only: promote/demote between ADMIN and CUSTOMER. Can't touch SUPERADMIN accounts (including your own). */
	public AdminUserDto updateRole(int id, Role newRole) {
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
		target = repo.save(target);
		Customer customer = target.getCustomerId() == null ? null : customerRepo.findById(target.getCustomerId().intValue());
		return toAdminDto(target, customer);
	}
}
