package use_case.security;

/** Abstraction for safely hashing and checking account passwords. */
public interface PasswordHasher {
    /** Returns an encoded salted password hash. */
    String hash(String plainPassword);

    /** Checks a supplied password against a previously stored encoded hash. */
    boolean matches(String plainPassword, String encodedHash);
}
