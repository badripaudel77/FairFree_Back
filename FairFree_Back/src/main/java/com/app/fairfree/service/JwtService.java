package com.app.fairfree.service;

import com.app.fairfree.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JwtService {

    @Autowired
    private JwtUtil jwtUtil;

    public String generateToken(String email, Set<String> roles) {
        return jwtUtil.generateAccessToken(email, roles);
    }
}
