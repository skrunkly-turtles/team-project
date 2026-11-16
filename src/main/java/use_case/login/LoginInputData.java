package use_case.login;

/** Credentials submitted from the login screen. */
public final class LoginInputData {
    private final String username;
    private final String password;

    /** Stores the submitted credentials. */
    public LoginInputData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /** Returns the supplied username. */
    public String getUsername() { return username; }

    /** Returns the supplied password. */
    public String getPassword() { return password; }
}
