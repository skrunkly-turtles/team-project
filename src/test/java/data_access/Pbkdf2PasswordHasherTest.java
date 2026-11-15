package data_access;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests salted cryptographic password hashing and verification. */
class Pbkdf2PasswordHasherTest {
    /** Two hashes of one password should be different because salts differ. */
    @Test
    void saltsPasswordHashesIndependently() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        String first = hasher.hash("longsecret123");
        String second = hasher.hash("longsecret123");
        assertNotEquals(first, second);
        assertTrue(hasher.matches("longsecret123", first));
        assertTrue(hasher.matches("longsecret123", second));
    }

    /** Incorrect, missing or malformed values cannot authenticate. */
    @Test
    void rejectsBadPasswordsAndMalformedHashes() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        String hash = hasher.hash("longsecret123");
        assertFalse(hasher.matches("wrongsecret", hash));
        assertFalse(hasher.matches("longsecret123", "plain-text"));
        assertFalse(hasher.matches(null, hash));
    }
}
