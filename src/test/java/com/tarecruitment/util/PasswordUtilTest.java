package com.tarecruitment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashPasswordCanBeVerifiedWithOriginalPassword() {
        String hashed = PasswordUtil.hashPassword("SecurePass123");

        assertTrue(PasswordUtil.verify("SecurePass123", hashed));
        assertFalse(PasswordUtil.verify("wrong-password", hashed));
    }

    @Test
    void legacyMd5PasswordsAreStillAcceptedAndMarkedForMigration() {
        String legacyHash = PasswordUtil.encrypt("oldPassword");

        PasswordUtil.VerificationResult result = PasswordUtil.verifyResult("oldPassword", legacyHash);

        assertTrue(result.isVerified());
        assertTrue(result.needsMigration());
    }

    @Test
    void nullPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }
}
