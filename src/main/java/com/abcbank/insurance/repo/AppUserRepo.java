package com.abcbank.insurance.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.abcbank.insurance.entities.AppUser;

@Service
public interface AppUserRepo extends JpaRepository<AppUser, Integer> {
	AppUser findByFirebaseUid(String firebaseUid);
	AppUser findByEmail(String email);
	List<AppUser> findAllByOrderByCreatedOnDesc();
}
