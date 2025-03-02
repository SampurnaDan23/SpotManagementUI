package com.qpa.dto;

import com.qpa.entity.UserRole;
import java.util.Set;

public class RegisterDTO {
    private String username;
    private String password;
    private Set<UserRole> roles;
    
    public RegisterDTO() {
		
	}

	public RegisterDTO(String username, String password, Set<UserRole> roles) {
		super();
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

	public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }
}