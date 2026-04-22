package com.example.unimagdalena.TiendaEcommerce.security.web;

import com.example.unimagdalena.TiendaEcommerce.security.dto.AuthDtos.AuthResponse;
import com.example.unimagdalena.TiendaEcommerce.security.dto.AuthDtos.LoginRequest;
import com.example.unimagdalena.TiendaEcommerce.security.dto.AuthDtos.RegisterRequest;
import com.example.unimagdalena.TiendaEcommerce.security.entity.AppUser;
import com.example.unimagdalena.TiendaEcommerce.security.jwt.JwtService;
import com.example.unimagdalena.TiendaEcommerce.security.repository.AppUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByEmailIgnoreCase(request.email())) {
            return ResponseEntity.badRequest().build();
        }

        AppUser user = AppUser.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of("ROLE_USER"))
                .build();

        appUserRepository.save(user);

        var principal = User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(
                principal,
                Map.of("roles", user.getRoles())
        );

        return ResponseEntity.ok(
                new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AppUser user = appUserRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow();

        var principal = User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(
                principal,
                Map.of("roles", user.getRoles())
        );

        return ResponseEntity.ok(
                new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds())
        );
    }
}