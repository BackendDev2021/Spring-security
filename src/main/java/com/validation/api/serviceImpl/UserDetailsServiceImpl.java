package com.validation.api.serviceImpl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cyberazon.jewellery.model.AppUser;
import com.cyberazon.jewellery.request.SignUpRequest;
import com.cyberazon.jewellery.response.AppResponse;
import com.cyberazon.jewellery.service.LoginService;
import com.validation.api.exception.AppException;
import com.validation.api.exception.ErrorDetails;
import com.validation.api.response.LoginResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService, LoginService {

	
	@Autowired
	private AuthenticationManager authenticationManager;

	
	@Autowired
	com.validation.api.repo.UserRepo<com.validation.api.model.User> userRepo;


	@Autowired
	private com.validation.api.security.JwtTokenUtil jwtTokenUtil;

	@Override
	public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {

		AppUser user = utilityService.getUser(emailId);
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
		return new User(user.getEmailId(), user.getPassword(), authorities);
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

	@Override
	public LoginResponse login(String emailId, String password) {

		if (com.amazonaws.util.StringUtils.isNullOrEmpty(emailId)
				&& com.amazonaws.util.StringUtils.isNullOrEmpty(password)) {
			log.error("VALUES_NOT_FOUND");
			throw new AppException(ErrorDetails.VALUES_NOT_FOUND);
		}
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	
		if (passwordEncoder.matches(password, user.getPassword())) {
			try {
				if (user.getActive() == 0) {
					throw new AppException(ErrorDetails.ACCOUNT_ACTIVATION_ERROR);
				}
				authenticate(emailId, password);
				return new AppResponse<>(Constants.SUCCESS, Constants.SUCCESS_CODE, getLoginResponse(user));

			} catch (Exception e) {
				throw new AppException(ErrorDetails.ACCOUNT_ACTIVATION_ERROR);
			}
		}

		throw new AppException(ErrorDetails.USER_NOT_FOUND);
	}

	@Override
	public LoginResponse signUp(@Valid SignUpRequest request) {

		Optional<User> userRequest = userRepo.findByEmailId(request.getEmailId());
		if (userRequest.isPresent()) {
			log.error("USER_ALREADY_EXISTS");
			throw new AppException(ErrorDetails.USER_EXISTS);
		}

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		//AppUser user = userMapper.toAppUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setActivationCode(getRandomCode(20));
		userRepo.save(user);
		return getLoginResponse(user);
	}

	private LoginResponse getLoginResponse(AppUser user) {

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

		Map<String, Object> claims = new HashMap<>();
		claims.put("name", user.getFirstName());
		claims.put("role", user.getRole().name());

		final String token = jwtTokenUtil.generateToken(new User(user.getEmailId(), user.getPassword(), authorities),
				claims);

		com.validation.api.response.LoginResponse response = new com.validation.api.response.LoginResponse();
		response.setToken(token);
		response.setUserName(user.getFirstName());
		response.setEmailId(user.getEmailId());
		response.setRole(user.getRole().name());

		if (user.getForceChange() == 1) {
			response.setMessage("FORCE_PASSWORD");
		} else {
			response.setMessage("SUCCESS");
		}
		return response;
	}

}
