package com.qpa.dto;

import com.qpa.entity.PriceType;
import com.qpa.entity.SpotType;
import com.qpa.entity.VehicleType;

public class SpotSearchCriteria {
    private String city;
    private String area;
    private String street;
    private SpotType spotType;
    private Boolean hasEVCharging;
    private PriceType priceType;
    private VehicleType supportedVehicleType;
    
	public SpotSearchCriteria() {

	}

	public SpotSearchCriteria(String city, String area, String street, SpotType spotType, Boolean hasEVCharging,
			PriceType priceType, VehicleType supportedVehicleType) {
		super();
		this.city = city;
		this.area = area;
		this.street = street;
		this.spotType = spotType;
		this.hasEVCharging = hasEVCharging;
		this.priceType = priceType;
		this.supportedVehicleType = supportedVehicleType;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public SpotType getSpotType() {
		return spotType;
	}

	public void setSpotType(SpotType spotType) {
		this.spotType = spotType;
	}

	public Boolean getHasEVCharging() {
		return hasEVCharging;
	}

	public void setHasEVCharging(Boolean hasEVCharging) {
		this.hasEVCharging = hasEVCharging;
	}

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public VehicleType getSupportedVehicleType() {
		return supportedVehicleType;
	}

	public void setSupportedVehicleType(VehicleType supportedVehicleType) {
		this.supportedVehicleType = supportedVehicleType;
	}
}
