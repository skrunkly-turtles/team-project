package use_case.login;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import data_access.Pbkdf2PasswordHasher;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests authentication and active-user session behavior. */
class LoginInteractorTest {
    /** Collects success/failure without a graphical interface. */
    private static final class Output implements LoginOutputBoundary {
        LoginOutputData success;
        String error;
        @Override public void prepareSuccessView(LoginOutputData data) { success = data; }
        @Override public void prepareFailView(String message) { error = message; }
    }

    /** Authenticates valid credentials and opens a session. */
    @Test
    void acceptsCorrectPassword() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        db.save(new CommonUserFactory().create("reader123", hasher.hash("password123")));
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        Output output = new Output();
        new LoginInteractor(db, output, hasher, session)
                .execute(new LoginInputData("reader123", "password123"));
        assertEquals("reader123", output.success.getUsername());
        assertEquals("reader123", session.getCurrentUsername());
    }

    /** Rejects wrong passwords without authenticating a user. */
    @Test
    void rejectsWrongPassword() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        InMemoryUserDataAccessObject db = new InMemoryUserDataAccessObject();
        db.save(new CommonUserFactory().create("reader123", hasher.hash("password123")));
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        Output output = new Output();
        new LoginInteractor(db, output, hasher, session)
                .execute(new LoginInputData("reader123", "notmysecret"));
        assertNull(output.success);
        assertNotNull(output.error);
        assertNull(session.getCurrentUsername());
    }

    /** Uses the same generic error for unknown accounts. */
    @Test
    void rejectsUnknownUsername() {
        Output output = new Output();
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        new LoginInteractor(new InMemoryUserDataAccessObject(), output,
                new Pbkdf2PasswordHasher(), session)
                .execute(new LoginInputData("doesnotexist", "secret123"));
        assertEquals("Invalid username or password.", output.error);
        assertNull(session.getCurrentUsername());
    }

    /** Requires logout before switching authenticated accounts. */
    @Test
    void preventsSilentAccountSwitching() {
        Output output = new Output();
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        session.signIn("firstUser");
        new LoginInteractor(new InMemoryUserDataAccessObject(), output,
                new Pbkdf2PasswordHasher(), session)
                .execute(new LoginInputData("secondUser", "password123"));
        assertNotNull(output.error);
        assertEquals("firstUser", session.getCurrentUsername());
    }
}
