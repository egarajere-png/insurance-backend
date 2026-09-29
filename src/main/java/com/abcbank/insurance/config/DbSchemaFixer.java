package com.abcbank.insurance.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate's ddl-auto=update only ADDS tables/columns; it never widens or
 * relaxes existing ones. Databases created before the gender fix therefore
 * still have customer.gender as varchar(1) NOT NULL, which is what produced
 * "value too long" / "null value violates not-null" / HTTP 500 errors.
 *
 * These statements are idempotent and run on every start. Each one is
 * isolated so a failure (e.g. table not created yet) never stops the app.
 * For production, move them to Flyway/Liquibase migrations.
 */
@Slf4j
@Component
public class DbSchemaFixer implements ApplicationRunner {

	private final JdbcTemplate jdbc;

	public DbSchemaFixer(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void run(ApplicationArguments args) {
		String[] statements = {
				// gender: widen + allow null
				"ALTER TABLE customer ALTER COLUMN gender TYPE varchar(16)",
				"ALTER TABLE customer ALTER COLUMN gender DROP NOT NULL",
				// legacy single-letter values -> full words
				"UPDATE customer SET gender = 'Male' WHERE upper(gender) = 'M'",
				"UPDATE customer SET gender = 'Female' WHERE upper(gender) = 'F'",
				// applications created before the review workflow existed
				"UPDATE customer_product SET status = 'PENDING_REVIEW' WHERE status IS NULL",
				"UPDATE customer_product SET submitted_on = created_on WHERE submitted_on IS NULL",
				// dependantsNo was never maintained; recompute (person_type: 1 = DEPENDANT, 3 = BOTH)
				"UPDATE customer c SET dependants_no = (SELECT COUNT(*) FROM dependant d "
						+ "WHERE d.customer_id = c.id AND d.person_type IN (1, 3))" };
		for (String sql : statements) {
			try {
				jdbc.execute(sql);
			} catch (Exception e) {
				log.warn("Schema fix skipped [{}]: {}", sql, e.getMessage());
			}
		}
		log.info("Schema fixes applied.");
	}
}
