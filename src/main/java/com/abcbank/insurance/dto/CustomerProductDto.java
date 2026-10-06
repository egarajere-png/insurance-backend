package com.abcbank.insurance.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CustomerProductDto {
	private int id;
	private boolean paymentMade;
	private boolean inGoodHealth;
	private String healthStatus;
	private boolean specificDiasgnosis;
	private String specificDiasgnosisStatus;
	private int customerId;
	private int productId;
	/** IDs of the customer's dependant/beneficiary records to cover under this application. May be empty. */
	private List<Integer> coveredPersonIds = new ArrayList<>();
}