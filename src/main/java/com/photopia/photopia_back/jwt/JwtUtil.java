package com.photopia.photopia_back.jwt;

import com.photopia.photopia_back.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer("Photopia")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7200_000)) // normalement c'est 2heures mais vasy vas attendre 2heures pour voir si c'est bon
                .signWith(secretKey)
                .compact();
    }

}
