package se.fulkopinglibrary.fulkopinglibrary.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    
    public static final String PASSWORD_REQUIREMENTS = 
        "Password must be at least 8 characters long and contain:\n" +
        "- At least one uppercase letter (A-Z)\n" +
        "- At least one lowercase letter (a-z)\n" +
        "- At least one number (0-9)\n" +
        "- At least one special character (!@#$%^&*)";

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

    public static String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");
        
        if (!hasUppercase) {
            return "Password must contain at least one uppercase letter (A-Z)";
        }
        if (!hasLowercase) {
            return "Password must contain at least one lowercase letter (a-z)";
        }
        if (!hasNumber) {
            return "Password must contain at least one number (0-9)";
        }
        if (!hasSpecial) {
            return "Password must contain at least one special character (!@#$%^&*)";
        }
        
        return null; // null means password meets all requirements
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
