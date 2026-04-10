package com.tarecruitment.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {
    private static final String PBKDF2_PREFIX = "PBKDF2$";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int PBKDF2_SALT_LENGTH = 16;
    private static final int PBKDF2_HASH_BITS = 256;
    private static final Pattern LEGACY_MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");

    public static String encrypt(String password) {
        return legacyMd5(password);
    }

    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try {
            byte[] salt = new byte[PBKDF2_SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_HASH_BITS);
            return PBKDF2_PREFIX
                    + PBKDF2_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verify(String password, String encryptedPassword) {
        return verifyResult(password, encryptedPassword).isVerified();
    }

    public static VerificationResult verifyResult(String password, String storedPassword) {
        if (password == null || storedPassword == null || storedPassword.trim().isEmpty()) {
            return VerificationResult.failed();
        }
        String stored = storedPassword.trim();
        if (isPbkdf2Hash(stored)) {
            return verifyPbkdf2(password, stored)
                    ? VerificationResult.success(false)
                    : VerificationResult.failed();
        }
        if (isLegacyMd5Hash(stored)) {
            return legacyMd5(password).equalsIgnoreCase(stored)
                    ? VerificationResult.success(true)
                    : VerificationResult.failed();
        }
        return VerificationResult.failed();
    }

    public static boolean isLegacyMd5Hash(String storedPassword) {
        return storedPassword != null && LEGACY_MD5_PATTERN.matcher(storedPassword.trim()).matches();
    }

    public static String generateSalt() {
        byte[] salt = new byte[PBKDF2_SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String legacyMd5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    private static boolean isPbkdf2Hash(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PBKDF2_PREFIX);
    }

    private static boolean verifyPbkdf2(String password, String stored) {
        String[] parts = stored.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    public static class VerificationResult {
        private final boolean verified;
        private final boolean needsMigration;

        private VerificationResult(boolean verified, boolean needsMigration) {
            this.verified = verified;
            this.needsMigration = needsMigration;
        }

        public boolean isVerified() {
            return verified;
        }

        public boolean needsMigration() {
            return needsMigration;
        }

        public static VerificationResult success(boolean needsMigration) {
            return new VerificationResult(true, needsMigration);
        }

        public static VerificationResult failed() {
            return new VerificationResult(false, false);
        }
    }
}
