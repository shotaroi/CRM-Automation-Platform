package com.shotaroi.crm.application.auth;

import com.shotaroi.crm.domain.UserRole;
import com.shotaroi.crm.domain.entity.Tenant;
import com.shotaroi.crm.domain.entity.User;
import com.shotaroi.crm.infrastructure.persistence.TenantRepository;
import com.shotaroi.crm.infrastructure.persistence.UserRepository;
import com.shotaroi.crm.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResult register(UUID tenantId, String email, String password, UserRole role) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
        if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getTenantId(), user.getRole());
        return new AuthResult(token, user.getId(), user.getTenantId(), user.getRole());
    }

    public AuthResult login(UUID tenantId, String email, String password) {
        User user = userRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> new InvalidCredentialsException());
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId(), user.getTenantId(), user.getRole());
        return new AuthResult(token, user.getId(), user.getTenantId(), user.getRole());
    }

    public record AuthResult(String token, UUID userId, UUID tenantId, UserRole role) {}
}
