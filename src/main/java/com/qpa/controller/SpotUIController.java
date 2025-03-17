package com.qpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.dto.SpotStatistics;
import com.qpa.dto.LoginDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.entity.PriceType;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.SpotType;
import com.qpa.entity.User;
import com.qpa.entity.VehicleType;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SpotUIController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8080/api"; // Backend URL
    private final int SESSION_TIMEOUT = 60 * 60 * 24 * 7; // 7 days in seconds

    private List<String> cities = List.of(
        "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Kolkata", "Pune", "Jaipur",
        "Ahmedabad", "Lucknow", "Kanpur", "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam",
        "Patna", "Vadodara", "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", "Meerut",
        "Rajkot", "Varanasi", "Srinagar", "Aurangabad", "Dhanbad", "Amritsar", "Allahabad",
        "Ranchi", "Coimbatore", "Jabalpur", "Gwalior", "Vijayawada", "Jodhpur", "Madurai",
        "Raipur", "Kota", "Guwahati", "Chandigarh", "Solapur", "Hubli–Dharwad", "Bareilly",
        "Moradabad", "Mysore", "Tiruchirappalli", "Tiruppur", "Dehradun", "Jalandhar", 
        "Aligarh", "Bhubaneswar", "Salem", "Warangal", "Guntur", "Bhiwandi", "Saharanpur",
        "Gorakhpur", "Bikaner", "Amravati", "Noida", "Jamshedpur", "Bhilai", "Cuttack",
        "Firozabad", "Kochi", "Nellore", "Bhavnagar", "Jammu", "Udaipur", "Davangere", 
        "Bellary", "Kurnool", "Malegaon", "Kolhapur", "Ajmer", "Anantapur", "Erode",
        "Rourkela", "Tirunelveli", "Akola", "Latur", "Panipat", "Mathura", "Kollam",
        "Bilaspur", "Shimoga", "Chandrapur", "Junagadh", "Thrissur", "Alwar", "Bardhaman",
        "Kakinada", "Nizamabad", "Parbhani", "Tumkur", "Hisar", "Kharagpur", "Nanded",
        "Ichalkaranji", "Bathinda", "Shahjahanpur", "Rampur", "Ratlam", "Hapur", "Rewa"
    );

    // Home Page
    @GetMapping("/")
    public String landingPage(HttpServletRequest request) {
        // Check for auto-login via cookie
        User user = getUserFromCookie(request);
        if (user != null) {
            request.getSession().setAttribute("currentUser", user);
        }
        
        return "redirect:/home";
    }

    // Home Page
    @GetMapping("/home")
    public String homePage(HttpServletRequest request, Model model) {
        // Check for auto-login via cookie
        User user = getUserFromCookie(request);
        if (user != null) {
            request.getSession().setAttribute("currentUser", user);
        }
        
        // Check if user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "home";
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        // Check for auto-login via cookie
        User user = getUserFromCookie(request);
        if (user != null) {
            request.getSession().setAttribute("currentUser", user);
        }
        
        // If already logged in, redirect to home
        if (request.getSession().getAttribute("currentUser") != null) {
            return "redirect:/home";
        }
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }

    // Show Register Page
    @GetMapping("/register")
    public String showRegisterPage(HttpServletRequest request, Model model) {
        // Check for auto-login via cookie
        User user = getUserFromCookie(request);
        if (user != null) {
            request.getSession().setAttribute("currentUser", user);
        }
        
        // If already logged in, redirect to home
        if (request.getSession().getAttribute("currentUser") != null) {
            return "redirect:/home";
        }
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterDTO registerDTO, Model model, 
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<RegisterDTO> httpRequest = new HttpEntity<>(registerDTO, headers);
            
            ResponseEntity<User> httpResponse = restTemplate.postForEntity(
                BASE_URL + "/auth/register", 
                httpRequest, 
                User.class
            );

            if (httpResponse.getStatusCode().is2xxSuccessful()) {
                // Automatically log in after successful registration
                User registeredUser = httpResponse.getBody();
                
                // Set user in session
                HttpSession session = request.getSession(true);
                session.setAttribute("currentUser", registeredUser);
                session.setMaxInactiveInterval(SESSION_TIMEOUT);
                
                // Set persistent cookie
                Cookie loginCookie = createLoginCookie(registeredUser);
                response.addCookie(loginCookie);
                
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
    public String loginUser(@ModelAttribute LoginDTO loginDTO, Model model,
                           HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<LoginDTO> httpRequest = new HttpEntity<>(loginDTO, headers);
            
            ResponseEntity<User> httpResponse = restTemplate.postForEntity(
                BASE_URL + "/auth/login", 
                httpRequest, 
                User.class
            );

            if (httpResponse.getStatusCode().is2xxSuccessful()) {
                // Store user in session
                User loggedInUser = httpResponse.getBody();
                
                // Set user in session
                HttpSession session = request.getSession(true);
                session.setAttribute("currentUser", loggedInUser);
                session.setMaxInactiveInterval(SESSION_TIMEOUT);
                
                // Set persistent cookie
                Cookie loginCookie = createLoginCookie(loggedInUser);
                response.addCookie(loginCookie);
                
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
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Remove user from session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
            session.invalidate();
        }
        
        // Delete the cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginInfo".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        
        return "redirect:/login";
    }

    @PostMapping("/spots/create")
    public String createSpot(
        @ModelAttribute SpotCreateDTO spotCreateDTO, 
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
        Model model, 
        HttpServletRequest request
    ) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Add userId to the spot creation request
            spotCreateDTO.setOwner(currentUser);

            // Prepare the request with multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("spot", spotCreateDTO);
            body.add("userId", currentUser.getId());
            
            // Add image file if present
            if (!imageFile.isEmpty() || imageFile != null) {
                body.add("image", imageFile.getResource());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<SpotResponseDTO> response = restTemplate.postForEntity(
                BASE_URL + "/spots/create",
                requestEntity,
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
    public String showCreateSpotPage(Model model, HttpServletRequest request) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("spotCreateDTO", new SpotCreateDTO());
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("cities", cities);

        return "spot_create";
    }

    // Show Spot List Page
    @GetMapping("/spots/list")
    public String viewAllSpots(Model model, HttpServletRequest request) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        SpotResponseDTO[] spots = restTemplate.getForObject(BASE_URL + "/spots/all", SpotResponseDTO[].class);

        for (SpotResponseDTO spot : spots) {
            if (spot.getSpotImage() != null) {
                spot.setSpotImageBase64(convertImageToBase64(spot.getSpotImage()));
            }
        }

        model.addAttribute("spots", spots);
        return "spot_list";
    }

    @GetMapping("/spots/search")
    public String searchSpots(
        @RequestParam(required = false) String city,
        @RequestParam(required = false) SpotType spotType,
        @RequestParam(required = false) Boolean hasEVCharging,
        @RequestParam(required = false) VehicleType supportedVehicleType,
        @RequestParam(required = false) SpotStatus status,
        Model model,
        HttpServletRequest request
    ) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Prepare search criteria
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (city != null && !city.isEmpty()) params.add("city", city);
        if (spotType != null) params.add("spotType", spotType.toString());
        if (hasEVCharging != null) params.add("hasEVCharging", hasEVCharging.toString());
        if (supportedVehicleType != null) params.add("supportedVehicleType", supportedVehicleType.toString());
        if (status != null) params.add("status", status.toString());

        String queryParams = params
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue().get(0))
            .collect(Collectors.joining("&"));

        String urlWithParams = BASE_URL + "/spots/search?" + queryParams;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<SpotResponseDTO[]> response = restTemplate.exchange(
            urlWithParams,
            HttpMethod.GET, 
            null, 
            SpotResponseDTO[].class
        );

        SpotResponseDTO[] spots = response.getBody();

        // Convert images to Base64
        if (spots != null) {
            for (SpotResponseDTO spot : spots) {
                if (spot.getSpotImage() != null) {
                    spot.setSpotImageBase64(convertImageToBase64(spot.getSpotImage()));
                }
            }
        }
        
        model.addAttribute("spots", spots);
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("status", SpotStatus.values());
        model.addAttribute("cities", cities);
        return "search_spot";
    }

    @GetMapping("/spots/statistics")
    public String getSpotStatistics(Model model, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        SpotStatistics statistics = restTemplate.getForObject(
            BASE_URL + "/spots/statistics", 
            SpotStatistics.class
        );

        model.addAttribute("statistics", statistics);
        return "statistics";
    }

    // Owner's Spots Page
    @GetMapping("/spots/owner")
    public String getOwnerSpots(Model model, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        SpotResponseDTO[] spots = restTemplate.getForObject(
            BASE_URL + "/spots/owner?userId=" + currentUser.getId(), 
            SpotResponseDTO[].class
        );

        // Convert images to Base64
        if (spots != null) {
            for (SpotResponseDTO spot : spots) {
                if (spot.getSpotImage() != null) {
                    spot.setSpotImageBase64(convertImageToBase64(spot.getSpotImage()));
                }
            }
        }

        model.addAttribute("spots", spots);
        return "owner_spots";
    }


    // Show Edit Spot Form
    @GetMapping("/spots/edit/{spotId}")
    public String showEditSpotForm(@PathVariable Long spotId, Model model, HttpServletRequest request) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Fetch the spot details from the backend
            ResponseEntity<SpotResponseDTO> response = restTemplate.getForEntity(
                BASE_URL + "/spots/" + spotId,
                SpotResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                SpotResponseDTO spot = response.getBody();
                
                // Convert spot images to base64 for display
                if (spot.getSpotImage() != null) {
                    spot.setSpotImageBase64(convertImageToBase64(spot.getSpotImage()));
                }
                
                // Check if the current user is the owner of this spot
                if (!spot.getOwner().getId().equals(currentUser.getId())) {
                    return "redirect:/spots/owner?error=unauthorized";
                }
                
                model.addAttribute("spot", spot);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("spotTypes", SpotType.values());
                model.addAttribute("priceTypes", PriceType.values());
                model.addAttribute("vehicleTypes", VehicleType.values());
                model.addAttribute("cities", cities);
                model.addAttribute("status", SpotStatus.values());
                
                return "update_spot";
            } else {
                return "redirect:/spots/owner?error=spotNotFound";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving spot: " + e.getMessage());
            return "redirect:/spots/owner?error=retrievalError";
        }
    }

    // Process Spot Update
    @PostMapping("/spots/update")
    public String updateSpot(
        @RequestParam Long spotId,
        @ModelAttribute SpotCreateDTO spotCreateDTO,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
        @RequestParam Long userId,
        Model model,
        HttpServletRequest request
    ) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Verify the user is authorized to update this spot
        if (!userId.equals(currentUser.getId())) {
            return "redirect:/spots/owner?error=unauthorized";
        }
        
        try {    
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Add image if provided
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("spot", spotCreateDTO);
            body.add("userId", userId);
            body.add("spotId", spotId);

            if (imageFile != null && !imageFile.isEmpty()) {
                body.add("image", imageFile.getResource());
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Send update request to backend
            ResponseEntity<SpotResponseDTO> response = restTemplate.exchange(
                BASE_URL + "/spots/" + spotId,
                HttpMethod.PUT,
                requestEntity,
                SpotResponseDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/spots/owner?success=spotUpdated";
            } else {
                model.addAttribute("error", "Failed to update spot");
                return "redirect:/spots/edit/" + spotId + "?error=updateFailed";
            }
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", "Update failed: " + errorMessage);
            return "redirect:/spots/edit/" + spotId + "?error=" + errorMessage;
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "redirect:/spots/edit/" + spotId + "?error=" + e.getMessage();
        }
    }

    // Process Spot Delete
    @GetMapping("/spots/delete/{spotId}")
    public String deleteSpot(
        @PathVariable Long spotId,
        HttpServletRequest request,
        Model model
    ) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            // Get the spot to verify ownership
            ResponseEntity<SpotResponseDTO> getResponse = restTemplate.getForEntity(
                BASE_URL + "/spots/" + spotId,
                SpotResponseDTO.class
            );
            
            if (getResponse.getStatusCode().is2xxSuccessful()) {
                SpotResponseDTO spot = getResponse.getBody();
                
                // Check if the current user is the owner
                if (!spot.getOwner().getId().equals(currentUser.getId())) {
                    return "redirect:/spots/owner?error=unauthorizedDelete";
                }
                
                // Make delete request to backend with userId as parameter
                String deleteUrl = BASE_URL + "/spots/" + spotId + "?userId=" + currentUser.getId();
                restTemplate.delete(deleteUrl);
                
                return "redirect:/spots/owner?success=spotDeleted";
            } else {
                return "redirect:/spots/owner?error=spotNotFound";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting spot: " + e.getMessage());
            return "redirect:/spots/owner?error=deletionError";
        }
    }

    @GetMapping(value="/spots/toggle")

    public String toggleSpotActivation(

        @RequestParam Long spotId,

        HttpServletRequest request

    ) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            // Call the backend API to toggle spot activation
            ResponseEntity<SpotResponseDTO> response = restTemplate.exchange(

                    BASE_URL + "/spots/toggle/" + spotId,

                    HttpMethod.PUT,

                    null,

                    SpotResponseDTO.class

                );
            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/spots/owner?success=statusToggled";
            } else {
                return "redirect:/spots/owner?error=toggleFailed";
            }
        } catch (Exception e) {
            return "redirect:/spots/owner?error=" + e.getMessage();
        }
    }

    // Helper methods for persistent login
    private Cookie createLoginCookie(User user) {
        // Create a cookie with user ID and a simple encoded credential
        // In a production app, you would use more secure encoding and possibly JWT
        String userInfo = user.getId() + ":" + Base64.getEncoder().encodeToString(
            (user.getPassword()).getBytes());
        
        Cookie cookie = new Cookie("loginInfo", userInfo);
        cookie.setMaxAge(SESSION_TIMEOUT);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // Prevent JavaScript access
        // cookie.setSecure(true); // Uncomment for HTTPS
        
        return cookie;
    }
    
    private User getUserFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginInfo".equals(cookie.getName())) {
                    try {
                        String value = cookie.getValue();
                        String[] parts = value.split(":", 2);
                        
                        if (parts.length == 2) {
                            Long userId = Long.parseLong(parts[0]);
                            
                            // Use the user ID to fetch fresh user data from the backend
                            ResponseEntity<User> response = restTemplate.getForEntity(
                                BASE_URL + "/auth/user/" + userId, 
                                User.class
                            );
                            
                            if (response.getStatusCode().is2xxSuccessful()) {
                                return response.getBody();
                            }
                        }
                    } catch (Exception e) {
                        // Log the error but continue
                        System.err.println("Error parsing login cookie: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        return null;
    }

    public String convertImageToBase64(byte[] image) {   
        return Base64.getEncoder().encodeToString(image);
    }
    
    @GetMapping("/spots/by-booking")
    public String getSpotByBookingIdPage(@RequestParam(required = false) Long bookingId, Model model, HttpServletRequest request) {
        System.out.println("Navigating to search spot by Booking ID page. Booking ID: " + bookingId);

        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bookingId == null) {
            return "search_spot_bookingId";
        }

        try {
            String url = BASE_URL + "/spots/by-booking/" + bookingId;
            System.out.println("Calling backend API: " + url);

            ResponseEntity<SpotResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, null, SpotResponseDTO.class);

            SpotResponseDTO spot = response.getBody();

            if (spot != null) {
                System.out.println("Spot found: " + spot.getSpotNumber());

                if (spot.getSpotImage() != null) {
                    String base64Image = "data:image/png;base64," + convertImageToBase64(spot.getSpotImage());
                    spot.setSpotImageBase64(base64Image);
                    System.out.println("Spot image converted to Base64.");
                }
            }

            model.addAttribute("spot", spot);
        } catch (HttpClientErrorException.NotFound ex) {
            System.out.println("Error: " + ex.getResponseBodyAsString());
            model.addAttribute("errorMessage", "No spot found for booking ID: " + bookingId);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            model.addAttribute("errorMessage", "An error occurred while fetching spot details.");
        }

        return "search_spot_bookingId";
    }


}