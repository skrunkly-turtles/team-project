package interface_adapter.home;

/** The authenticated username displayed by the temporary home screen. */
public final class HomeState {
    private final String username;

    /** Stores the username (null when no one is logged in). */
    public HomeState(String username) { this.username = username; }

    /** Returns the current username or null. */
    public String getUsername() { return username; }
}
