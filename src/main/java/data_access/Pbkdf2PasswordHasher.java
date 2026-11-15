package data_access;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import use_case.security.PasswordHasher;

/** Implements salted PBKDF2-HMAC-SHA256 password hashing using the standard JDK. */
public final class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Returns a different salted encoded hash each time it is called. */
    @Override
    public String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(plainPassword, salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Verifies the submitted password using constant-time byte comparison. */
    @Override
    public boolean matches(String plainPassword, String encodedHash) {
        if (plainPassword == null || encodedHash == null) { return false; }
        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) { return false; }
            int iterations = Integer.parseInt(parts[1]);
            // Prevent untrusted file content from triggering excessive CPU usage.
            if (iterations < 10_000 || iterations > 1_000_000) { return false; }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length != SALT_LENGTH || expected.length != HASH_BITS / 8) { return false; }
            byte[] actual = derive(plainPassword, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Derives the hash bytes and destroys the password spec after use. */
    private static byte[] derive(String password, byte[] salt, int iterations) {
        if (password == null) { throw new IllegalArgumentException("Password cannot be null."); }
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
