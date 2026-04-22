package com.example.unimagdalena.TiendaEcommerce.security.config;

import com.example.unimagdalena.TiendaEcommerce.security.entity.AppUser;
import com.example.unimagdalena.TiendaEcommerce.security.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String email = "admin@test.com";

            if (!appUserRepository.existsByEmailIgnoreCase(email)) {
                AppUser admin = AppUser.builder()
                        .email(email)
                        .password(passwordEncoder.encode("1234"))
                        .roles(Set.of("ROLE_ADMIN"))
                        .build();

                appUserRepository.save(admin);
            }
        };
    }
}