package use_case.login;

/** Non-sensitive result of successful authentication. */
public final class LoginOutputData {
    private final String username;

    /** Stores the username allowed to enter the application. */
    public LoginOutputData(String username) { this.username = username; }

    /** Returns the authenticated username. */
    public String getUsername() { return username; }
}
