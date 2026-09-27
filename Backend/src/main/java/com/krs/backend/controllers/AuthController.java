package com.krs.backend.controllers;
import com.krs.backend.models.User;
import com.krs.backend.repositories.UserRepository;
import com.krs.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired AuthenticationManager authenticationManager;
    @Autowired JwtUtils jwtUtils;
    @Autowired UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, Object> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String role = authentication.getAuthorities().stream()
                .findFirst().map(item -> item.getAuthority()).orElse("USER");

        // Fetch the stored password hash to embed fingerprint in token
        User user = userRepository.findUserByUsername(authentication.getName());
        String passwordHash = user != null ? user.getPassword() : "";

        String jwt = jwtUtils.generateJwtToken(authentication.getName(), role, passwordHash);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", authentication.getName());
        return response;
    }
}
