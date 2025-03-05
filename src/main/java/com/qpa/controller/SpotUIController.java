package com.qpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.dto.LoginDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.entity.PriceType;
import com.qpa.entity.Spot;
import com.qpa.entity.SpotType;
import com.qpa.entity.User;
import com.qpa.entity.VehicleType;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Controller
public class SpotUIController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8080/api"; // Backend URL

    // Home Page
    @GetMapping("/")
    public String landingPage() {
        return "redirect:/home";
    }

    // Home Page
    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "home";
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginPage(HttpSession session, Model model) {
        // If already logged in, redirect to home
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/home";
        }
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }

    // Show Register Page
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session, Model model) {
        // If already logged in, redirect to home
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/home";
        }
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterDTO registerDTO, Model model, HttpSession session) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<RegisterDTO> request = new HttpEntity<>(registerDTO, headers);
            
            ResponseEntity<User> response = restTemplate.postForEntity(
                BASE_URL + "/auth/register", 
                request, 
                User.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // Automatically log in after successful registration
                User registeredUser = response.getBody();
                session.setAttribute("currentUser", registeredUser);
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Registration unsuccessful");
                return "register";
            }
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", "Registration failed: " + errorMessage);
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/perform_login")
    public String loginUser(@ModelAttribute LoginDTO loginDTO, Model model, HttpSession session) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<LoginDTO> request = new HttpEntity<>(loginDTO, headers);
            
            ResponseEntity<User> response = restTemplate.postForEntity(
                BASE_URL + "/auth/login", 
                request, 
                User.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // Store user in session
                User loggedInUser = response.getBody();
                session.setAttribute("currentUser", loggedInUser);
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Login unsuccessful");
                return "login";
            }
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", "Login failed: " + errorMessage);
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "login";
        }
    }

    // Logout endpoint
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Remove user from session
        session.removeAttribute("currentUser");
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/spots/create")
    public String createSpot(
        @ModelAttribute SpotCreateDTO spotCreateDTO, 
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
        Model model, 
        HttpSession session
    ) {
        // Ensure user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Add userId to the spot creation request
            spotCreateDTO.setOwner(currentUser);

            // Create a MultipartFile list for backend compatibility
            List<MultipartFile> images = new ArrayList<>();
            if (imageFile != null && !imageFile.isEmpty()) {
                images.add(imageFile);
            }

            // Prepare the request with multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("spot", spotCreateDTO);
            body.add("userId", currentUser.getId());
            
            // Add image file if present
            if (!images.isEmpty()) {
                body.add("images", imageFile.getResource());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<SpotResponseDTO> response = restTemplate.postForEntity(
                BASE_URL + "/spots/create",
                requestEntity,  // Use requestEntity instead of spotCreateDTO
                SpotResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/spots/list?spotCreationSuccess";
            } else {
                model.addAttribute("error", "Spot creation failed");
                return "spot_create";
            }
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", "Spot creation failed: " + errorMessage);
            return "spot_create";
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "spot_create";
        }
    }

    // Show Spot Creation Page
    @GetMapping("/spots/create")
    public String showCreateSpotPage(Model model, HttpSession session) {
        // Ensure user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("spotCreateDTO", new SpotCreateDTO());
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());

        return "spot_create";
    }

    // Show Spot List Page
    @GetMapping("/spots/list")
    public String viewAllSpots(Model model, HttpSession session) {
        // Ensure user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        SpotResponseDTO[] spots = restTemplate.getForObject(BASE_URL + "/spots/all", SpotResponseDTO[].class);

        for (SpotResponseDTO spot : spots) {
            if (spot.getSpotImages() != null) {
                spot.setSpotImagesBase64(convertImagesToBase64(spot.getSpotImages()));
            }
        }

        model.addAttribute("spots", spots);
        return "spot_list";
    }

    public List<String> convertImagesToBase64(List<byte[]> images) {
        List<String> base64Images = new ArrayList<>();
        for (byte[] image : images) {
            base64Images.add(Base64.getEncoder().encodeToString(image));
        }
        return base64Images;
    }
}