package com.vpp.cc.controller;

import com.vpp.cc.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Api(value = "Battery Management System Authentication", tags = "Auth API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @ApiOperation(value = "Authencticate User", notes = "Provide a token to authenticated user.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();

        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            response.put("error", "Username is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = jwtTokenProvider.generateToken(username);
        if (token == null || token.isEmpty()) {
            response.put("error", "Invalid username");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
    }
}
