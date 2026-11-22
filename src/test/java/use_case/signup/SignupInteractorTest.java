package use_case.signup;

import data_access.InMemoryUserDataAccessObject;
import data_access.Pbkdf2PasswordHasher;
import models.CommonUserFactory;
import models.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Checks accepted and rejected signup attempts. */
class SignupInteractorTest {
    /** Captures output boundary messages without creating a Swing window. */
    private static final class Output implements SignupOutputBoundary {
        SignupOutputData success;
        String error;
        @Override public void prepareSuccessView(SignupOutputData data) { success = data; }
        @Override public void prepareFailView(String message) { error = message; }
    }

    /** Constructs a test signup use case. */
    private SignupInteractor signup(InMemoryUserDataAccessObject db, Output output) {
        return new SignupInteractor(db, output, new CommonUserFactory(), new Pbkdf2PasswordHasher());
    }

    /** Saves an account with a hashed rather than plaintext password. */
    @Test
    void createsAccountWithPasswordHash() {
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        Output output = new Output();
        signup(db, output).execute(new SignupInputData("reader123", "longsecret99", "longsecret99"));
        assertNotNull(output.success);
        assertEquals("reader123", output.success.getUsername());
        User saved = db.get("reader123");
        assertNotNull(saved);
        assertNotEquals("longsecret99", saved.getPasswordHash());
        assertTrue(new Pbkdf2PasswordHasher().matches("longsecret99", saved.getPasswordHash()));
    }

    /** Rejects repeated usernames without replacing the existing account. */
    @Test
    void rejectsDuplicateUsername() {
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        Output output = new Output();
        SignupInteractor interactor = signup(db, output);
        interactor.execute(new SignupInputData("reader123", "longsecret99", "longsecret99"));
        output.success = null;
        interactor.execute(new SignupInputData("reader123", "different99", "different99"));
        assertNull(output.success);
        assertTrue(output.error.contains("already exists"));
    }

    /** Rejects a short or badly formatted username. */
    @Test
    void rejectsInvalidUsername() {
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        Output output = new Output();
        signup(db, output).execute(new SignupInputData("1x", "longsecret99", "longsecret99"));
        assertNull(output.success);
        assertNotNull(output.error);
        assertFalse(db.existsByName("1x"));
    }

    /** Requires the two password fields to match. */
    @Test
    void rejectsDifferentPasswords() {
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        Output output = new Output();
        signup(db, output).execute(new SignupInputData("reader123", "longsecret99", "notthesame"));
        assertTrue(output.error.contains("do not match"));
        assertFalse(db.existsByName("reader123"));
    }

    /** Requires at least eight characters in a password. */
    @Test
    void rejectsShortPassword() {
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        Output output = new Output();
        signup(db, output).execute(new SignupInputData("reader123", "short", "short"));
        assertNotNull(output.error);
        assertFalse(db.existsByName("reader123"));
    }
}
