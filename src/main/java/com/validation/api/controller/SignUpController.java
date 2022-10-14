package com.validation.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.validation.api.request.SignupRequest;
import com.validation.api.response.LoginRequest;
import com.validation.api.response.LoginResponse;
import com.validation.api.service.LoginService;

@RestController
public class SignUpController {

	@Autowired
	private LoginService loginService;

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		return loginService.login(request.getEmailId(), request.getPassword());
	}

	@PostMapping("/signup")
	public LoginResponse signUp(@Valid @RequestBody SignupRequest request) {
		return loginService.signUp(request);
	}
}
