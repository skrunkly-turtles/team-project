package use_case.session;

/** Stores the username of the user currently authenticated in this application. */
public interface CurrentUserSession {
    /** Sets the active username after successful authentication. */
    void signIn(String username);

    /** Clears the current user and any authenticated state. */
    void signOut();

    /** Returns the current username, or null when logged out. */
    String getCurrentUsername();
}
