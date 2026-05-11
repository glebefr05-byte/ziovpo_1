package com.licensing.config;

import com.licensing.model.enums.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.licensing.model.enums.JwtTokenType.ACCESS;
import static com.licensing.model.enums.JwtTokenType.REFRESH;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;

    @Value("${jwt.accessSecret}")
    private String accessSecret;

    @Value("${jwt.refreshSecret}")
    private String refreshSecret;

    @Value("${jwt.accessExpiration}")
    private long accessExpiration;

    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    public String createAccessToken(String email, Set<GrantedAuthority> authorities, String sessionId) {
        return buildToken(email, authorities, sessionId, ACCESS);
    }

    public String createRefreshToken(String email, String sessionId) {
        return buildToken(email, Collections.emptySet(), sessionId, REFRESH);
    }

    private String buildToken(String email,
                              Set<GrantedAuthority> authorities,
                              String sessionId,
                              JwtTokenType JwtTokenType) {
        long expiration = (JwtTokenType == ACCESS) ? accessExpiration : refreshExpiration;

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("authorities", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));
        claims.put("token_type", JwtTokenType);
        claims.put("session_id", sessionId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean validateTokenType(String token, String expectedType) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get("token_type", String.class);
            return expectedType.equals(tokenType);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getSessionIdFromToken(String token) {
        return getClaimsFromToken(token).get("session_id", String.class);
    }

    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("token_type", String.class);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Set<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Collection<?> authoritiesCol = claims.get("authorities", Collection.class);
        return authoritiesCol.stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toSet());
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}