package com.isaacmicheal.payflow.auth;

import com.isaacmicheal.payflow.auth.dto.AuthResponse;
import com.isaacmicheal.payflow.auth.dto.LoginRequest;
import com.isaacmicheal.payflow.auth.dto.RegisterRequest;
import com.isaacmicheal.payflow.common.ConflictException;
import com.isaacmicheal.payflow.domain.Merchant;
import com.isaacmicheal.payflow.domain.Role;
import com.isaacmicheal.payflow.repository.MerchantRepository;
import com.isaacmicheal.payflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MerchantRepository merchants;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(MerchantRepository merchants,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.merchants = merchants;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (merchants.existsByEmail(request.email())) {
            throw new ConflictException("A merchant with that email already exists");
        }
        Merchant merchant = new Merchant();
        merchant.setName(request.name());
        merchant.setEmail(request.email());
        merchant.setPasswordHash(passwordEncoder.encode(request.password()));
        merchant.setRole(Role.MERCHANT);
        merchants.save(merchant);

        String token = jwtService.generateToken(merchant.getEmail(), merchant.getRole().name());
        return AuthResponse.bearer(token, merchant.getEmail(), merchant.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        Merchant merchant = merchants.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(merchant.getEmail(), merchant.getRole().name());
        return AuthResponse.bearer(token, merchant.getEmail(), merchant.getRole().name());
    }
}
