package com.bankymono.business_logic_server_demo.service;

import com.bankymono.business_logic_server_demo.model.OtpAuthentication;
import com.bankymono.business_logic_server_demo.model.UsernamePasswordAuthentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class InitialAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationManager manager;

    @Value("${jwt.signing.key}")
    private String signingKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = request.getHeader("username");
        String password = request.getHeader("password");
        String code = request.getHeader("code");

        if (code == null) {
            Authentication a =
                    new UsernamePasswordAuthentication(username, password);
            SecretKey signature = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.info(new ObjectMapper().writeValueAsString(signature.getEncoded()));
            manager.authenticate(a);
        } else {
            Authentication a =
                    new OtpAuthentication(username, code);

            a = manager.authenticate(a);

            SecretKey key = Keys.hmacShaKeyFor(
                    signingKey.getBytes(
                            StandardCharsets.UTF_8));

            String jwt = Jwts.builder()
                    .setClaims(Map.of("username", username))
                    .signWith(key)
                    .compact();

            response.setHeader("Authorization", jwt);
        }


    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        return !request.getServletPath()
                .equals("/login");
    }
}
