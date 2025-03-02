package com.qpa.dto;

import com.qpa.entity.UserRole;
import java.util.Set;

public class AuthResponse {
    private Long userId;
    private String username;
    private Set<UserRole> roles;
    private String message;

    public AuthResponse() {
		
	}

	public AuthResponse(Long userId, String username, Set<UserRole> roles, String message) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

