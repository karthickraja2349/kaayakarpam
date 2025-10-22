package com.kaayakarpam.auth.controller;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    /*private static final String SECRET = System.getenv("SSO_SECRET") != null ?
            System.getenv("SSO_SECRET") :
            "PLEASE_CHANGE_THIS_TO_A_REAL_SECRET_OF_MIN_32_BYTES";*/
    private static final String SECRET = "12345678901234567890123456789012";        
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(int userId, String role, String email, int minutesValid) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + minutesValid * 60L * 1000L);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("email", email)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public static int getUserId(String token) {
        Claims c = parseToken(token).getBody();
        return Integer.parseInt(c.getSubject());
    }

    public static String getRole(String token) {
        return (String) parseToken(token).getBody().get("role");
    }

    public static String getEmail(String token) {
        return (String) parseToken(token).getBody().get("email");
    }
}

