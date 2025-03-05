package com.qpa.dto;

import java.util.List;
import java.util.Set;

import com.qpa.entity.PriceType;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.SpotType;
import com.qpa.entity.VehicleType;

public class SpotResponseDTO {
	private Long spotId;
	private String spotNumber;
	private SpotType spotType;
	private SpotStatus status;
	private LocationDTO location;
	private boolean hasEVCharging;
	private double price;
	private PriceType priceType;
	private Double rating;
	private List<byte[]> spotImages;
	private Set<VehicleType> supportedVehicleTypes;
	private List<String> spotImagesBase64;
	
	public SpotResponseDTO() {

	}

	public SpotResponseDTO(Long spotId, String spotNumber, SpotType spotType, SpotStatus status, LocationDTO location,
			boolean hasEVCharging, double price, PriceType priceType, Double rating,
			List<byte[]> spotImages, Set<VehicleType> supportedVehicleTypes, List<String> spotImagesBase64) {
		super();
		this.spotId = spotId;
		this.spotNumber = spotNumber;
		this.spotType = spotType;
		this.status = status;
		this.location = location;
		this.hasEVCharging = hasEVCharging;
		this.price = price;
		this.priceType = priceType;
		this.rating = rating;
		this.spotImages = spotImages;
		this.supportedVehicleTypes = supportedVehicleTypes;
		this.spotImagesBase64 = spotImagesBase64;
	}

	public Long getSpotId() {
		return spotId;
	}

	public void setSpotId(Long spotId) {
		this.spotId = spotId;
	}

	public String getSpotNumber() {
		return spotNumber;
	}

	public void setSpotNumber(String spotNumber) {
		this.spotNumber = spotNumber;
	}

	public SpotType getSpotType() {
		return spotType;
	}

	public void setSpotType(SpotType spotType) {
		this.spotType = spotType;
	}

	public SpotStatus getStatus() {
		return status;
	}

	public void setStatus(SpotStatus status) {
		this.status = status;
	}

	public LocationDTO getLocation() {
		return location;
	}

	public void setLocation(LocationDTO location) {
		this.location = location;
	}

	public boolean getHasEVCharging() {
		return hasEVCharging;
	}

	public void setHasEVCharging(boolean hasEVCharging) {
		this.hasEVCharging = hasEVCharging;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public List<byte[]> getSpotImages() {
		return spotImages;
	}

	public void setSpotImages(List<byte[]> spotImages) {
		this.spotImages = spotImages;
	}

	public Set<VehicleType> getSupportedVehicleTypes() {
		return supportedVehicleTypes;
	}

	public void setSupportedVehicleTypes(Set<VehicleType> supportedVehicleTypes) {
		this.supportedVehicleTypes = supportedVehicleTypes;
	}

	public List<String> getSpotImagesBase64() {
		return spotImagesBase64;
	}

	public void setSpotImagesBase64(List<String> spotImagesBase64) {
		this.spotImagesBase64 = spotImagesBase64;
	}
}
