package com.abcbank.insurance.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes the Firebase Admin SDK on startup so FirebaseAuthFilter can
 * verify ID tokens. Needs a service account key — see README / the
 * application.yml comment for `firebase.credentials-path`.
 *
 * Deliberately non-fatal: if the credentials file is missing (e.g. a fresh
 * checkout before Pambi has added one), the app still starts — every request
 * requiring auth will just fail with a clear 503 instead of the whole
 * application refusing to boot. Remove this tolerance once credentials are
 * always present in every environment.
 */
@Slf4j
@Component
public class FirebaseConfig {

	@Value("${firebase.credentials-path:firebase-service-account.json}")
	private String credentialsPath;

	public static volatile boolean initialized = false;

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		try {
			InputStream serviceAccount = openCredentials(credentialsPath);
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}
			initialized = true;
			log.info("Firebase Admin SDK initialized.");
		} catch (Exception e) {
			initialized = false;
			log.warn("Firebase Admin SDK NOT initialized ({}). Auth-protected endpoints will return 503 until a "
					+ "valid service account is provided at '{}' (see application.yml: firebase.credentials-path).",
					e.getMessage(), credentialsPath);
		}
	}

	private InputStream openCredentials(String path) throws IOException {
		// Supports an absolute/relative file path or a classpath resource.
		try {
			return new FileInputStream(path);
		} catch (IOException fileNotFound) {
			return new ClassPathResource(path).getInputStream();
		}
	}
}
