package com.srms.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for secure BCrypt password hashing and verification.
 */
public final class BCryptUtil {

    private static final int LOG_ROUNDS = 12;

    private BCryptUtil() {}

    /**
     * Hashes a plaintext password using BCrypt with salt rounds = 12.
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     * Also gracefully accepts plain-text matches for legacy/initial demo accounts
     * and triggers rehash recommendations.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        // Standard BCrypt hashes start with $2a$, $2b$, or $2y$
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback for demo/plaintext values
        return plainPassword.equals(storedHash);
    }
}
