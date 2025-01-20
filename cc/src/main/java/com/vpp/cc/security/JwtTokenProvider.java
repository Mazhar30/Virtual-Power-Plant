package com.vpp.cc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${jwt.token.expiration.time}")
    private long EXPIRATION_TIME;

    @Value("${jwt.users}")
    private String usersProperty;

    public List<String> getUsers() {
        return Arrays.asList(usersProperty.split(","));
    }

    public String generateToken(String username) {
        if (getUsers().contains(username)) {
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, username);
        } else {
            return "";
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Use the SecretKey instance
                .compact();
    }

    public Boolean isValidToken(String token) {
        if (!isTokenExpired(token)) {
            return getUsers().contains(extractUsername(token));
        }
        return false;
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()) // Use the SecretKey instance
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // Get the signing key used to sign the token
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}