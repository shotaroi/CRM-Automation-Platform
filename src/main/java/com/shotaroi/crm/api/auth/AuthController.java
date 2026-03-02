package com.shotaroi.crm.api.auth;

import com.shotaroi.crm.application.auth.AuthService;
import com.shotaroi.crm.application.auth.EmailAlreadyExistsException;
import com.shotaroi.crm.application.auth.InvalidCredentialsException;
import com.shotaroi.crm.application.auth.TenantNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(
                request.tenantId(),
                request.email(),
                request.password(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(
                request.tenantId(),
                request.email(),
                request.password()
        );
        return ResponseEntity.ok(toResponse(result));
    }

    private AuthResponse toResponse(AuthService.AuthResult result) {
        return new AuthResponse(
                result.token(),
                result.userId(),
                result.tenantId(),
                result.role()
        );
    }
}
