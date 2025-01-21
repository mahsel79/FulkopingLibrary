package se.fulkopinglibrary.fulkopinglibrary.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    // Generate a random salt
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hash the password with SHA-256 and salt
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // Verify a password against a stored hash and salt
    public static boolean verifyPassword(String password, String storedHash, String salt) {
        String hashedPassword = hashPassword(password, salt);
        return hashedPassword.equals(storedHash);
    }
    
    public static String legacyHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public static void main(String[] args) {
        // Generate hashes for test users
        String aliceSalt = generateSalt();
        String aliceHash = hashPassword("password1", aliceSalt);
        
        String bobSalt = generateSalt();
        String bobHash = hashPassword("password2", bobSalt);
        
        System.out.println("Alice:");
        System.out.println("Salt: " + aliceSalt);
        System.out.println("Hash: " + aliceHash);
        System.out.println();
        System.out.println("Bob:");
        System.out.println("Salt: " + bobSalt);
        System.out.println("Hash: " + bobHash);
    }
}
