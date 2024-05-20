package com.example.EduvibeBackend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.PostRegistroDto;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.service.impl.UserService;

@RestController
public class UserController {
	
	@Autowired
	UserService userService;
	
	@GetMapping("/user/existeEmail")
	public ResponseEntity<?> existeEmail(@RequestParam Optional<String> email){
		User user = null;
		ResponseEntity result = null;
		try {
			user = userService.getUserByEmail(email.orElse(null));
		} catch (Exception e) {
			throw new GlobalException("Error al encontrar usuario");
		}
		
		if(user==null) {
			result = ResponseEntity.ok(null);
		}else {
			result = ResponseEntity.ok(PostRegistroDto.user(user));
		}
		return result;
	}
	

	
}
