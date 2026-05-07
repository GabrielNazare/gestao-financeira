package com.gestaofinanceira.service;

import com.gestaofinanceira.model.User;
import com.gestaofinanceira.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final GoogleIdTokenVerifier verifier;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
                       @Value("${google.client.id}") String googleClientId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-financeira")
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtExpiration))
                .subject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String authenticateWithGoogle(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            User user = userRepository.findById(email).orElseGet(() -> {
                User newUser = new User(email, name, picture);
                return userRepository.save(newUser);
            });

            return generateToken(user);
        } else {
            throw new RuntimeException("Token do Google inválido");
        }
    }

    public String login(String email, String password) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        return generateToken(user);
    }

    public String register(String email, String password, String name) {
        Optional<User> existingUser = userRepository.findById(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("E-mail já está em uso");
        }

        User newUser = new User(email, name, null);
        newUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(newUser);

        return generateToken(newUser);
    }
}

