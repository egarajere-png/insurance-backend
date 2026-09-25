package com.abcbank.insurance.dto;

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
}