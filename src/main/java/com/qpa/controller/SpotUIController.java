package com.qpa.controller;


import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.AuthResponse;
import com.qpa.dto.LoginDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.entity.*;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SpotUIController {

    private static final String BASE_URL = "http://localhost:8080/api/spots/"; 
    private static final String AUTH_API_URL = "http://localhost:8080/api/auth/";

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/")
    public String login() {
        
        return "login";
    }

    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("roles", UserRole.values()); 
        return "register";  
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String[] roles,
            Model model) {

        
        Set<UserRole> roleSet = new HashSet<>();
        for (String role : roles) {
            roleSet.add(UserRole.valueOf(role)); 
        }

        
        RegisterDTO registerDTO = new RegisterDTO(username, password, roleSet);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterDTO> request = new HttpEntity<>(registerDTO, headers);

        
        ResponseEntity<String> response = restTemplate.postForEntity(AUTH_API_URL+"register", request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("message", "User registered successfully!");
        } else {
            model.addAttribute("error", "Registration failed.");
        }

        return "register"; 
    }
    
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        LoginDTO loginDTO = new LoginDTO(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(loginDTO, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(AUTH_API_URL+"login", request, AuthResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return "home"; 
            }
        } catch (Exception e) {
        	model.addAttribute("error", "Login failed.");
        }

        return "login"; 
    }
    
    
    @GetMapping("/spots/create")
    public String createSpot(Model model) {
        model.addAttribute("spotCreateDTO", new SpotCreateDTO());

        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());
        
        //System.out.println("Vehicle Types: " + Arrays.toString(VehicleType.values()));

        return "createSpot";  
    }
    
    @PostMapping("/spots/create")
    public String createSpot(
            @ModelAttribute SpotCreateDTO spotCreateDTO,
            @RequestParam("images") List<MultipartFile> images,
            Model model) {

        spotCreateDTO.setImages(images);  

        // Prepare the multi-part request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("spot", spotCreateDTO);

        try {
            for (MultipartFile image : images) {
                body.add("images", new InputStreamResource(image.getInputStream()) {
                    @Override
                    public String getFilename() {  // This is now valid
                        return image.getOriginalFilename();
                    }
                });
            }
        } catch (IOException e) {
            model.addAttribute("error", e.getMessage());
            return "createSpot"; 
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<SpotResponseDTO> response = restTemplate.postForEntity(BASE_URL + "create", request, SpotResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return "redirect:/home"; // Redirect to home on success
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error creating spot. Please try again.");
        }

        return "createSpot"; 
    }

    
    
    
    
    
    
    
    
//    @PostMapping("/login")
//    public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
//    	 // Create LoginDTO request
//        LoginDTO loginDTO = new LoginDTO(username, password);
//
//        // Set headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // Create request entity
//        HttpEntity<LoginDTO> requestEntity = new HttpEntity<>(loginDTO, headers);
//
//        try {
//            // Send POST request to backend
//            ResponseEntity<AuthResponse> response = restTemplate.exchange(
//                AUTH_API_URL+"login", HttpMethod.POST, requestEntity, AuthResponse.class);
//
//            // If login is successful, redirect to dashboard
//            return "redirect:/dashboard";
//        } catch (Exception e) {
//            // Handle login failure
//            model.addAttribute("error", "Invalid username or password");
//            return "login";
//        }
//    }
//    
//    @GetMapping("/register")
//    public String register() {
//    	return "register";
//    }
//    
//    @PostMapping("/register")
//    public String registerUser(
//            @RequestParam String username,
//            @RequestParam String password,
//            @RequestParam String role,
//            Model model) {
//
//        // Create RegisterDTO object
//        RegisterDTO registerDTO = new RegisterDTO();
//        registerDTO.setUsername(username);
//        registerDTO.setPassword(password);
//        registerDTO.setRoles(Collections.singleton(UserRole.valueOf(role)));
//
//        try {
//            restTemplate.postForObject(AUTH_API_URL+"register", registerDTO, String.class);
//            model.addAttribute("success", "Registration successful. You can now login.");
//        } catch (Exception e) {
//            model.addAttribute("error", "Registration failed. Try again.");
//        }
//
//        return "register";
//    }

//    @GetMapping("/addSpotForm")
//    public String addSpotForm(Model model) {
//        model.addAttribute("spot", new Spot());
//        model.addAttribute("spotTypes", Arrays.asList(SpotType.values()));
//        model.addAttribute("priceTypes", Arrays.asList(PriceType.values()));
//        model.addAttribute("vehicleTypes", Arrays.asList(VehicleType.values()));
//        return "addSpotForm";
//    }
//
//    @PostMapping("/addSpot")
//    public String addSpot(@ModelAttribute Spot spot,
//                          @RequestParam("images") List<MultipartFile> images) {
//        try {
//     
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("spot", spot);
//            
//            for (MultipartFile image : images) {
//                Resource fileResource = new ByteArrayResource(image.getBytes()) {
//                    @Override
//                    public String getFilename() {
//                        return image.getOriginalFilename();
//                    }
//                };
//                body.add("images", fileResource);
//            }
//
//            
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//            restTemplate.exchange(BASE_URL, HttpMethod.POST, requestEntity, Spot.class);
//
//            return "redirect:/addSpotForm?success";
//        } catch (Exception e) {
//            return "redirect:/addSpotForm?error";
//        }
//    }
}
