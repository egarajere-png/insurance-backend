package com.abcbank.insurance.dto;

import lombok.Data;

@Data
public class ProductDto {
	private int id;
	private String name;
	private String description;
	private int benefit;
	private int benefitChild;
	private String plan;
	private int premium;
	private int premiumAdditionalChild;
	private int premiumAdditionalAdultChild;
}