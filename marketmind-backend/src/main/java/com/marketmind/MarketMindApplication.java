package com.marketmind;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.crypto.SecretKey;

@SpringBootApplication
@EnableScheduling
public class MarketMindApplication {
    
    public static void main(String[] args) {
        // Generate a secure JWT secret if not provided
        String jwtSecret = System.getenv("MARKETMIND_JWT_SECRET");
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
            String base64Key = Encoders.BASE64.encode(key.getEncoded());
            System.setProperty("marketmind.jwt.secret", base64Key);
        }
        
        SpringApplication.run(MarketMindApplication.class, args);
    }
}
