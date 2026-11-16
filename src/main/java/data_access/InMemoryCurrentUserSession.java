package data_access;

import java.util.Objects;
import use_case.session.CurrentUserSession;

/** Stores one active login in memory until logout or application exit. */
public final class InMemoryCurrentUserSession implements CurrentUserSession {
    private String currentUsername;

    /** Sets the logged-in username. */
    @Override
    public synchronized void signIn(String username) {
        this.currentUsername = Objects.requireNonNull(username, "username");
    }

    /** Clears the logged-in username. */
    @Override
    public synchronized void signOut() { currentUsername = null; }

    /** Returns the username, or null when no user is logged in. */
    @Override
    public synchronized String getCurrentUsername() { return currentUsername; }
}
