package com.qpa.dto;

import java.util.Map;


import com.qpa.entity.SpotType;

public class SpotStatistics {
    private long totalSpots;
    private long availableSpots;
    private long unavailableSpots;
    private double averageRating;
    private Map<SpotType, Long> spotsByType;
    private Map<String, Long> spotsByCity;
    
	public SpotStatistics() {

	}

	public SpotStatistics(long totalSpots, long availableSpots, long unavailableSpots, double averageRating,
			Map<SpotType, Long> spotsByType, Map<String, Long> spotsByCity) {
		super();
		this.totalSpots = totalSpots;
		this.availableSpots = availableSpots;
		this.unavailableSpots = unavailableSpots;
		this.averageRating = averageRating;
		this.spotsByType = spotsByType;
		this.spotsByCity = spotsByCity;
	}

	public long getTotalSpots() {
		return totalSpots;
	}

	public void setTotalSpots(long totalSpots) {
		this.totalSpots = totalSpots;
	}

	public long getAvailableSpots() {
		return availableSpots;
	}

	public void setAvailableSpots(long availableSpots) {
		this.availableSpots = availableSpots;
	}

	public long getUnavailableSpots() {
		return unavailableSpots;
	}

	public void setUnavailableSpots(long unavailableSpots) {
		this.unavailableSpots = unavailableSpots;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public Map<SpotType, Long> getSpotsByType() {
		return spotsByType;
	}

	public void setSpotsByType(Map<SpotType, Long> spotsByType) {
		this.spotsByType = spotsByType;
	}

	public Map<String, Long> getSpotsByCity() {
		return spotsByCity;
	}

	public void setSpotsByCity(Map<String, Long> spotsByCity) {
		this.spotsByCity = spotsByCity;
	}
}