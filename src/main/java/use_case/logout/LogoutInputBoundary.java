package use_case.logout;

/** Input boundary for ending the current session. */
public interface LogoutInputBoundary {
    /** Ends the logged-in user's session. */
    void execute();
}
