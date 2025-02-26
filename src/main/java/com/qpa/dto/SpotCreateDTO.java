package com.qpa.dto;

import com.qpa.entity.PriceType;
import com.qpa.entity.SpotType;
import com.qpa.entity.User;
import com.qpa.entity.VehicleType;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

public class SpotCreateDTO {
	private String spotNumber;
	private SpotType spotType;
	private User owner;
	private LocationDTO location;
	private boolean hasEVCharging;
	private double price;
	private PriceType priceType;
	private List<DayOfWeek> availableDays;
	private List<MultipartFile> images;
	private Set<VehicleType> supportedVehicle;
	
	public SpotCreateDTO() {

	}

	public SpotCreateDTO(String spotNumber, SpotType spotType, User owner, LocationDTO location, boolean hasEVCharging, double price, PriceType priceType, List<DayOfWeek> availableDays, List<MultipartFile> images, Set<VehicleType> supportedVehicle) {
		this.spotNumber = spotNumber;
		this.spotType = spotType;
		this.owner = owner;
		this.location = location;
		this.hasEVCharging = hasEVCharging;
		this.price = price;
		this.priceType = priceType;
		this.availableDays = availableDays;
		this.images = images;
		this.supportedVehicle = supportedVehicle;
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

	public LocationDTO getLocation() {
		return location;
	}

	public void setLocation(LocationDTO location) {
		this.location = location;
	}

	public boolean isHasEVCharging() {
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

	public List<DayOfWeek> getAvailableDays() {
		return availableDays;
	}

	public void setAvailableDays(List<DayOfWeek> availableDays) {
		this.availableDays = availableDays;
	}

	public List<MultipartFile> getImages() {
		return images;
	}

	public void setImages(List<MultipartFile> images) {
		this.images = images;
	}

	public Set<VehicleType> getSupportedVehicle() {
		return supportedVehicle;
	}

	public void setSupportedVehicle(Set<VehicleType> supportedVehicle) {
		this.supportedVehicle = supportedVehicle;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
}
