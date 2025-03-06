package com.qpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SpotUIController {

	@Autowired
	private RestTemplate restTemplate;

	private final String BASE_URL = "http://localhost:8080/api"; // Backend URL
	 private final int SESSION_TIMEOUT = 60 * 60 * 24 * 7;

	private List<String> cities = List.of("Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Kolkata", "Pune",
			"Jaipur", "Ahmedabad", "Lucknow", "Kanpur", "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam", "Patna",
			"Vadodara", "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", "Meerut", "Rajkot", "Varanasi",
			"Srinagar", "Aurangabad", "Dhanbad", "Amritsar", "Allahabad", "Ranchi", "Coimbatore", "Jabalpur", "Gwalior",
			"Vijayawada", "Jodhpur", "Madurai", "Raipur", "Kota", "Guwahati", "Chandigarh", "Solapur", "Hubli–Dharwad",
			"Bareilly", "Moradabad", "Mysore", "Tiruchirappalli", "Tiruppur", "Dehradun", "Jalandhar", "Aligarh",
			"Bhubaneswar", "Salem", "Warangal", "Guntur", "Bhiwandi", "Saharanpur", "Gorakhpur", "Bikaner", "Amravati",
			"Noida", "Jamshedpur", "Bhilai", "Cuttack", "Firozabad", "Kochi", "Nellore", "Bhavnagar", "Jammu",
			"Udaipur", "Davangere", "Bellary", "Kurnool", "Malegaon", "Kolhapur", "Ajmer", "Anantapur", "Erode",
			"Rourkela", "Tirunelveli", "Akola", "Latur", "Panipat", "Mathura", "Kollam", "Bilaspur", "Shimoga",
			"Chandrapur", "Junagadh", "Thrissur", "Alwar", "Bardhaman", "Kakinada", "Nizamabad", "Parbhani", "Tumkur",
			"Hisar", "Kharagpur", "Nanded", "Ichalkaranji", "Bathinda", "Shahjahanpur", "Rampur", "Ratlam", "Hapur",
			"Rewa");

	// Home Page
	@GetMapping("/")
	public String landingPage() {
		return "redirect:/home";
	}

	// Home Page
	@GetMapping("/home")
	public String homePage(HttpSession session, Model model) {
		/*
		 * if (session.getAttribute("currentUser") != null) { return "redirect:/login";
		 * }
		 */
		return "home";
	}

	@GetMapping("/landing")
	public String showLandingPage(HttpSession session, Model model) {
		Object currentUser = session.getAttribute("currentUser");

		// Add user details to the model for Thymeleaf to display
		model.addAttribute("user", currentUser);

		return "landing"; // Redirect to landing.html
	}

	// Show Login Page
	@GetMapping("/login")
	public String showLoginPage(HttpSession session, Model model) {
		// If already logged in, redirect to landing page
		if (session.getAttribute("currentUser") != null) {
			return "redirect:/landing";
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

			ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/auth/register", request,
					User.class);

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

			ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/auth/login", request, User.class);

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
		return "redirect:/home";
	}

	@GetMapping("/smart-spots")
	public String exploreSmartSpots(HttpSession session) {
		// Check if the user is logged in
		if (session.getAttribute("currentUser") != null) {
			return "redirect:/landing"; // Redirect to landing page
		} else {
			return "redirect:/login"; // Redirect to login page
		}
	}

	@PostMapping("/spots/create")
	public String createSpot(@ModelAttribute SpotCreateDTO spotCreateDTO,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Model model,
			HttpSession session) {
		User currentUser = (User) session.getAttribute("currentUser");

		if (currentUser == null) {
			model.addAttribute("error", "You must be logged in to create a spot.");
			return "spot_create";
		}

		try {
			spotCreateDTO.setOwner(currentUser);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("spot", spotCreateDTO);
			body.add("userId", currentUser.getId());

			if (imageFile != null && !imageFile.isEmpty()) {
				body.add("images", imageFile.getResource());
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<SpotResponseDTO> response = restTemplate.postForEntity(BASE_URL + "/spots/create",
					requestEntity, SpotResponseDTO.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				return "redirect:/spots/list?spotCreationSuccess";
			} else {
				model.addAttribute("error", "Spot creation failed");
				return "spot_create";
			}
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
		model.addAttribute("cities", cities);

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

	@GetMapping("/spots/search")
	public String searchSpots(@RequestParam(required = false) String city,
			@RequestParam(required = false) SpotType spotType, @RequestParam(required = false) Boolean hasEVCharging,
			@RequestParam(required = false) VehicleType supportedVehicleType,
			@RequestParam(required = false) SpotStatus status, Model model, HttpSession session) {
		User currentUser = (User) session.getAttribute("currentUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		// Prepare search criteria
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		if (city != null && !city.isEmpty())
			params.add("city", city);
		if (spotType != null)
			params.add("spotType", spotType.toString());
		if (hasEVCharging != null)
			params.add("hasEVCharging", hasEVCharging.toString());
		if (supportedVehicleType != null)
			params.add("supportedVehicleType", supportedVehicleType.toString());
		if (status != null)
			params.add("status", status.toString());

		String queryParams = params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue().get(0))
				.collect(Collectors.joining("&"));

		String urlWithParams = BASE_URL + "/spots/search?" + queryParams;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);

		ResponseEntity<SpotResponseDTO[]> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, null,
				SpotResponseDTO[].class);

		SpotResponseDTO[] spots = response.getBody();

		// Convert images to Base64
		if (spots != null) {
			for (SpotResponseDTO spot : spots) {
				if (spot.getSpotImages() != null) {
					spot.setSpotImagesBase64(convertImagesToBase64(spot.getSpotImages()));
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
	public String getSpotStatistics(Model model, HttpSession session) {
		User currentUser = (User) session.getAttribute("currentUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		SpotStatistics statistics = restTemplate.getForObject(BASE_URL + "/spots/statistics", SpotStatistics.class);

		model.addAttribute("statistics", statistics);
		return "statistics";
	}

	// Owner's Spots Page
	@GetMapping("/spots/owner")
	public String getOwnerSpots(Model model, HttpSession session) {
		User currentUser = (User) session.getAttribute("currentUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		SpotResponseDTO[] spots = restTemplate.getForObject(BASE_URL + "/spots/owner?userId=" + currentUser.getId(),
				SpotResponseDTO[].class);

		// Convert images to Base64
		if (spots != null) {
			for (SpotResponseDTO spot : spots) {
				if (spot.getSpotImages() != null) {
					spot.setSpotImagesBase64(convertImagesToBase64(spot.getSpotImages()));
				}
			}
		}

		model.addAttribute("spots", spots);
		return "owner_spots";
	}

	public List<String> convertImagesToBase64(List<byte[]> images) {
		List<String> base64Images = new ArrayList<>();
		for (byte[] image : images) {
			base64Images.add(Base64.getEncoder().encodeToString(image));
		}
		return base64Images;
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
        //cookie.setSecure(true); // Uncomment for HTTPS
        
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

}