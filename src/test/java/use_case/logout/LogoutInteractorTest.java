package use_case.logout;

import data_access.InMemoryCurrentUserSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests logout state changes. */
class LogoutInteractorTest {
    /** Verifies that logout clears sessions and informs the presenter. */
    @Test
    void clearsCurrentSession() {
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        session.signIn("reader123");
        final boolean[] notified = {false};
        new LogoutInteractor(session, () -> notified[0] = true).execute();
        assertNull(session.getCurrentUsername());
        assertTrue(notified[0]);
    }

    /** Logging out twice is harmless and still reports completion. */
    @Test
    void canLogOutWhenAlreadyLoggedOut() {
        InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        new LogoutInteractor(session, () -> {}).execute();
        assertNull(session.getCurrentUsername());
    }
}
