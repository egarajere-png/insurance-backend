package com.abcbank.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
	private long totalCustomers;
	private long totalProducts;
	private long totalApplications;
	private long pendingReview;
	private long approved;
	private long rejected;
}
