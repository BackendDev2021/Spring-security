package com.validation.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler(AppException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse resourceNotFound(AppException ex) {
		ErrorResponse response = new ErrorResponse();

		if (ex.getResponseDetail() == null) {
			response.setStatus(400);
			response.setMessage(ex.getCustomMessage());
			response.setUiErrorKey("error_resource_exists_error");
			return response;
		}
		response.setStatus(ex.getResponseDetail().getCode());
		response.setMessage(ex.getResponseDetail().getMessage());
		response.setUiErrorKey(ex.getResponseDetail().getUiErrorKey());

		return response;
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	// public Map<String, String>
	// handleValidationExceptions(MethodArgumentNotValidException ex) {
	public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {

		// Map<String, String> errors = new HashMap<>();

		ErrorResponse response = new ErrorResponse();

		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();

			response.setStatus(400);
			response.setMessage(fieldName + " - " + errorMessage);
			response.setUiErrorKey("error_validation_failure");

			// errors.put(fieldName, errorMessage);
		});

		return response;
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(InternalError.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse internalException(AppException ex) {
		ErrorResponse response = new ErrorResponse();
		response.setStatus(ex.getResponseDetail().getCode());
		response.setMessage(ex.getResponseDetail().getMessage());
		response.setUiErrorKey(ex.getResponseDetail().getUiErrorKey());

		return response;
	}
}