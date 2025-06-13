package com.hometown.store.liquorstoreapp.security;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Argon2PasswordEncoder implements PasswordEncoder {
    
    private static final int SALT_LENGTH = 32;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // 64 MB
    private static final int PARALLELISM = 2;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        
        byte[] hash = generateHash(rawPassword.toString().getBytes(StandardCharsets.UTF_8), salt);
        
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            
            byte[] actualHash = generateHash(rawPassword.toString().getBytes(StandardCharsets.UTF_8), salt);
            
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    private byte[] generateHash(byte[] password, byte[] salt) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();
        
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password, hash);
        
        return hash;
    }
    
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return false;
    }
}