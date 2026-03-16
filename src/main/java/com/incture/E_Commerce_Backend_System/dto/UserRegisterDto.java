package com.incture.E_Commerce_Backend_System.dto;

public class UserRegisterDto {
	private long id;
	private String name;
	private String email;
	private String password;
	private String role;

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public long getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
