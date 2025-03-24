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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SpotUIController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8080/api";
    private final int SESSION_TIMEOUT = 60 * 60 * 24 * 7;

    // City list
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
            "Ichalkaranji", "Bathinda", "Shahjahanpur", "Rampur", "Ratlam", "Hapur", "Rewa");

    private Map<String, List<String>> stateCityMap = new HashMap<>() {
        {
            put("Maharashtra",
                    List.of("Mumbai", "Pune", "Nagpur", "Thane", "Nashik", "Aurangabad", "Solapur", "Bhiwandi",
                            "Amravati", "Kolhapur", "Akola", "Latur", "Nanded", "Ichalkaranji", "Parbhani",
                            "Chandrapur"));
            put("Delhi", List.of("Delhi", "New Delhi"));
            put("Karnataka",
                    List.of("Bangalore", "Mysore", "Hubli–Dharwad", "Davangere", "Bellary", "Shimoga", "Tumkur"));
            put("Telangana", List.of("Hyderabad", "Warangal", "Nizamabad", "Kakinada"));
            put("Tamil Nadu", List.of("Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Tiruppur", "Salem",
                    "Erode", "Tirunelveli"));
            put("West Bengal", List.of("Kolkata", "Bardhaman", "Kharagpur"));
            put("Rajasthan", List.of("Jaipur", "Jodhpur", "Kota", "Bikaner", "Ajmer", "Udaipur", "Alwar"));
            put("Gujarat", List.of("Ahmedabad", "Vadodara", "Rajkot", "Bhavnagar", "Junagadh"));
            put("Uttar Pradesh",
                    List.of("Lucknow", "Kanpur", "Ghaziabad", "Agra", "Meerut", "Varanasi", "Allahabad", "Bareilly",
                            "Moradabad", "Aligarh", "Saharanpur", "Gorakhpur", "Firozabad", "Mathura", "Shahjahanpur",
                            "Rampur", "Hapur"));
            put("Madhya Pradesh", List.of("Indore", "Bhopal", "Jabalpur", "Gwalior", "Ratlam", "Rewa"));
            put("Andhra Pradesh", List.of("Visakhapatnam", "Vijayawada", "Guntur", "Nellore", "Anantapur", "Kurnool"));
            put("Bihar", List.of("Patna", "Gaya", "Bhagalpur"));
            put("Haryana", List.of("Faridabad", "Hisar", "Panipat"));
            put("Punjab", List.of("Ludhiana", "Amritsar", "Jalandhar", "Bathinda"));
            put("Jammu and Kashmir", List.of("Srinagar", "Jammu"));
            put("Jharkhand", List.of("Dhanbad", "Ranchi", "Jamshedpur"));
            put("Chhattisgarh", List.of("Raipur", "Bhilai", "Bilaspur"));
            put("Assam", List.of("Guwahati"));
            put("Chandigarh", List.of("Chandigarh"));
            put("Uttarakhand", List.of("Dehradun"));
            put("Odisha", List.of("Bhubaneswar", "Cuttack", "Rourkela"));
            put("Kerala", List.of("Kochi", "Kollam", "Thrissur"));
        }
    };

    private Map<String, String> cityPincodeMap = new HashMap<>() {
        {
            put("Mumbai", "400001");
            put("Delhi", "110001");
            put("Bangalore", "560001");
            put("Hyderabad", "500001");
            put("Chennai", "600001");
            put("Kolkata", "700001");
            put("Pune", "411001");
            put("Jaipur", "302001");
            put("Ahmedabad", "380001");
            put("Lucknow", "226001");
            put("Kanpur", "208001");
            put("Nagpur", "440001");
            put("Indore", "452001");
            put("Thane", "400601");
            put("Bhopal", "462001");
            put("Visakhapatnam", "530001");
            put("Patna", "800001");
            put("Vadodara", "390001");
            put("Ghaziabad", "201001");
            put("Ludhiana", "141001");
            put("Agra", "282001");
            put("Nashik", "422001");
            put("Faridabad", "121001");
            put("Meerut", "250001");
            put("Rajkot", "360001");
            put("Varanasi", "221001");
            put("Srinagar", "190001");
            put("Aurangabad", "431001");
            put("Dhanbad", "826001");
            put("Amritsar", "143001");
            put("Allahabad", "211001");
            put("Ranchi", "834001");
            put("Coimbatore", "641001");
            put("Jabalpur", "482001");
            put("Gwalior", "474001");
            put("Vijayawada", "520001");
            put("Jodhpur", "342001");
            put("Madurai", "625001");
            put("Raipur", "492001");
            put("Kota", "324001");
            put("Guwahati", "781001");
            put("Chandigarh", "160001");
            put("Solapur", "413001");
            put("Hubli–Dharwad", "580001");
            put("Bareilly", "243001");
            put("Moradabad", "244001");
            put("Mysore", "570001");
            put("Tiruchirappalli", "620001");
            put("Tiruppur", "641601");
            put("Dehradun", "248001");
            put("Jalandhar", "144001");
            put("Aligarh", "202001");
            put("Bhubaneswar", "751001");
            put("Salem", "636001");
            put("Warangal", "506001");
            put("Guntur", "522001");
            put("Bhiwandi", "421308");
            put("Saharanpur", "247001");
            put("Gorakhpur", "273001");
            put("Bikaner", "334001");
            put("Amravati", "444601");
            put("Noida", "201301");
            put("Jamshedpur", "831001");
            put("Bhilai", "490001");
            put("Cuttack", "753001");
            put("Firozabad", "283203");
            put("Kochi", "682001");
            put("Nellore", "524001");
            put("Bhavnagar", "364001");
            put("Jammu", "180001");
            put("Udaipur", "313001");
            put("Davangere", "577001");
            put("Bellary", "583101");
            put("Kurnool", "518001");
            put("Malegaon", "423203");
            put("Kolhapur", "416001");
            put("Ajmer", "305001");
            put("Anantapur", "515001");
            put("Erode", "638001");
            put("Rourkela", "769001");
            put("Tirunelveli", "627001");
            put("Akola", "444001");
            put("Latur", "413512");
            put("Panipat", "132103");
            put("Mathura", "281001");
            put("Kollam", "691001");
            put("Bilaspur", "495001");
            put("Shimoga", "577201");
            put("Chandrapur", "442401");
            put("Junagadh", "362001");
            put("Thrissur", "680001");
            put("Alwar", "301001");
            put("Bardhaman", "713101");
            put("Kakinada", "533001");
            put("Nizamabad", "503001");
            put("Parbhani", "431401");
            put("Tumkur", "572101");
            put("Hisar", "125001");
            put("Kharagpur", "721301");
            put("Nanded", "431601");
            put("Ichalkaranji", "416115");
            put("Bathinda", "151001");
            put("Shahjahanpur", "242001");
            put("Rampur", "244901");
            put("Ratlam", "457001");
            put("Hapur", "245101");
            put("Rewa", "486001");
        }
    };

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
                    User.class);

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
                    User.class);

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
            HttpServletRequest request) {
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
            if (imageFile != null && !imageFile.isEmpty()) {
                body.add("image", imageFile.getResource());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<SpotResponseDTO> response = restTemplate.postForEntity(
                    BASE_URL + "/spots/create",
                    requestEntity,
                    SpotResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/spots/search?spotCreationSuccess";
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

        // Add states, cities, and pincode mappings
        model.addAttribute("states", stateCityMap.keySet());
        model.addAttribute("cities", cities);
        model.addAttribute("stateCityMap", stateCityMap);
        model.addAttribute("cityPincodeMap", cityPincodeMap);

        return "spot_create";
    }

    @GetMapping("/api/cities-by-state")
    @ResponseBody
    public List<Map<String, String>> getCitiesByState(@RequestParam String state) {
        List<String> stateCities = stateCityMap.getOrDefault(state, Collections.emptyList());

        List<Map<String, String>> result = new ArrayList<>();
        for (String city : stateCities) {
            Map<String, String> cityData = new HashMap<>();
            cityData.put("name", city);
            cityData.put("pincode", cityPincodeMap.getOrDefault(city, ""));
            result.add(cityData);
        }

        return result;
    }

    @GetMapping("/spots/search")
    public String combinedSpotsView(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) SpotType spotType,
            @RequestParam(required = false) Boolean hasEVCharging,
            @RequestParam(required = false) VehicleType supportedVehicleType,
            @RequestParam(required = false) SpotStatus status,
            Model model,
            HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Check if any filters are applied
        boolean hasFilters = city != null || spotType != null || hasEVCharging != null ||
                supportedVehicleType != null || status != null;

        SpotResponseDTO[] spots;

        if (hasFilters) {
            // If filters applied, use search endpoint
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

            String queryParams = params
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue().get(0))
                    .collect(Collectors.joining("&"));

            String urlWithParams = BASE_URL + "/spots/search?" + queryParams;

            try {
                ResponseEntity<SpotResponseDTO[]> response = restTemplate.exchange(
                        urlWithParams,
                        HttpMethod.GET,
                        null,
                        SpotResponseDTO[].class);
                spots = response.getBody();
            } catch (Exception e) {
                spots = new SpotResponseDTO[0]; // Empty array on error
                model.addAttribute("error", "Error fetching filtered spots: " + e.getMessage());
            }
        } else {
            // If no filters, get all spots
            try {
                spots = restTemplate.getForObject(BASE_URL + "/spots/all", SpotResponseDTO[].class);
            } catch (Exception e) {
                spots = new SpotResponseDTO[0]; // Empty array on error
                model.addAttribute("error", "Error fetching all spots: " + e.getMessage());
            }
        }

        // Add all necessary data to the model
        model.addAttribute("spots", spots);
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("status", SpotStatus.values());
        model.addAttribute("cities", cities);

        return "search_spots"; // This will be the name of your new Thymeleaf template
    }

    @GetMapping("/spots/statistics")
    public String getSpotStatistics(Model model, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        SpotStatistics statistics = restTemplate.getForObject(
                BASE_URL + "/spots/statistics",
                SpotStatistics.class);

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
                SpotResponseDTO[].class);

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
                    SpotResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                SpotResponseDTO spot = response.getBody();

                // No need to convert images to base64 for display

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
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!userId.equals(currentUser.getId())) {
            return "redirect:/spots/owner?error=unauthorized";
        }

        try {
            ResponseEntity<SpotResponseDTO> currentSpotResponse = restTemplate.getForEntity(
                    BASE_URL + "/spots/" + spotId,
                    SpotResponseDTO.class);

            if (currentSpotResponse.getStatusCode().is2xxSuccessful()) {
                SpotResponseDTO currentSpot = currentSpotResponse.getBody();

                if (spotCreateDTO.getSpotType() == null) {
                    spotCreateDTO.setSpotType(currentSpot.getSpotType());
                }

                if (spotCreateDTO.getLocation() == null) {
                    spotCreateDTO.setLocation(currentSpot.getLocation());
                } else {
                    if (spotCreateDTO.getLocation().getCity() == null) {
                        spotCreateDTO.getLocation().setCity(currentSpot.getLocation().getCity());
                    }
                    if (spotCreateDTO.getLocation().getState() == null) {
                        spotCreateDTO.getLocation().setState(currentSpot.getLocation().getState());
                    }
                    if (spotCreateDTO.getLocation().getPincode() == null) {
                        spotCreateDTO.getLocation().setPincode(currentSpot.getLocation().getPincode());
                    }
                    if (spotCreateDTO.getLocation().getLandmark() == null) {
                        spotCreateDTO.getLocation().setLandmark(currentSpot.getLocation().getLandmark());
                    }
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("spot", spotCreateDTO);
            body.add("userId", userId);
            body.add("spotId", spotId);

            if (imageFile != null && !imageFile.isEmpty()) {
                body.add("image", imageFile.getResource());
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<SpotResponseDTO> response = restTemplate.exchange(
                    BASE_URL + "/spots/" + spotId,
                    HttpMethod.PUT,
                    requestEntity,
                    SpotResponseDTO.class);

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
            Model model) {
        // Ensure user is logged in
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Get the spot to verify ownership
            ResponseEntity<SpotResponseDTO> getResponse = restTemplate.getForEntity(
                    BASE_URL + "/spots/" + spotId,
                    SpotResponseDTO.class);

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

    @GetMapping(value = "/spots/toggle")
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
                                    User.class);

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

    @GetMapping("/booked")
    public String getBookedSpots(Model model, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        String url = BASE_URL + "/spots/booked";

        try {
            ResponseEntity<SpotResponseDTO[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    SpotResponseDTO[].class);

            SpotResponseDTO[] bookedSpots = response.getBody();
            model.addAttribute("bookedSpots", bookedSpots);
        } catch (HttpClientErrorException.NotFound ex) {
            // Handle the 404 case - no booked spots found
            model.addAttribute("bookedSpots", new SpotResponseDTO[0]); // Empty array
            model.addAttribute("message", "No booked spots found.");
        }

        return "booked_spots_list";
    }

    @GetMapping("/spots/by-booking")
    public String getSpotByBookingIdPage(@RequestParam(required = false) Long bookingId, Model model,
            HttpServletRequest request) {
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

    @GetMapping("/spots/search-by-date")
    public String searchSpotsByDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model,
            HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        boolean hasDateFilters = startDate != null && !startDate.isEmpty()
                && endDate != null && !endDate.isEmpty();

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        List<SpotResponseDTO> spots = new ArrayList<>();

        if (hasDateFilters) {
            try {
                StringBuilder urlBuilder = new StringBuilder(
                        BASE_URL + "/spots/by-booking?startDate=" + startDate + "&endDate=" + endDate);

                String url = urlBuilder.toString();

                ResponseEntity<SpotResponseDTO[]> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        SpotResponseDTO[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    spots = Arrays.asList(response.getBody());
                    model.addAttribute("spotsFound", spots.size() > 0);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error searching for spots: " + e.getMessage());
                model.addAttribute("spotsFound", false);
            }
        } else {
            model.addAttribute("spotsFound", null);
        }

        model.addAttribute("spots", spots);
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("cities", cities);

        return "search_spots_by_date";
    }
}