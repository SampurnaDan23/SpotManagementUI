package com.qpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.entity.Spot;
import com.qpa.entity.VehicleType;
import com.qpa.entity.PriceType;
import java.util.Arrays;
import java.util.List;

@Controller
public class SpotUIController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:7213"; // Backend URL

    // Home Page
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Show Register Page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    // Show Spot Creation Page
    @GetMapping("/spots/create")
    public String showCreateSpotPage(Model model) {
        model.addAttribute("spotCreateDTO", new SpotCreateDTO());

        // Fetch dropdown values for spot types & price types
        List<String> spotTypes = Arrays.asList("Compact", "Standard", "Large");
        List<String> priceTypes = Arrays.asList("Hourly", "Daily", "Monthly");
        List<String> vehicleTypes = Arrays.asList("Car", "Bike", "EV");

        model.addAttribute("spotTypes", spotTypes);
        model.addAttribute("priceTypes", priceTypes);
        model.addAttribute("vehicleTypes", vehicleTypes);

        return "spot_create";
    }

    // Handle Spot Creation
    @PostMapping("/spots/create")
    public String createSpot(@ModelAttribute SpotCreateDTO spotCreateDTO, Model model) {
        restTemplate.postForEntity(BASE_URL + "/spots/create", spotCreateDTO, SpotCreateDTO.class);
        model.addAttribute("message", "Spot created successfully!");
        return "redirect:/spots/list";
    }

    // Show Spot List Page
    @GetMapping("/spots/list")
    public String viewAllSpots(Model model) {
        Spot[] spots = restTemplate.getForObject(BASE_URL + "/spots/list", Spot[].class);
        model.addAttribute("spots", spots);
        return "spot_list";
    }
}
