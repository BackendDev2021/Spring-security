package com.validation.api.service;

import javax.validation.Valid;

import com.validation.api.request.SignupRequest;
import com.validation.api.response.LoginResponse;

public class LoginService {


	public LoginResponse signUp(@Valid SignupRequest request);

	public LoginResponse login(String emailId, String password);
}
