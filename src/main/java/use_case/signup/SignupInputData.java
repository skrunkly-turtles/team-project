package use_case.signup;

/** Input collected from the signup screen. */
public final class SignupInputData {
    private final String username;
    private final String password;
    private final String repeatedPassword;

    /** Stores the three required registration fields. */
    public SignupInputData(String username, String password, String repeatedPassword) {
        this.username = username;
        this.password = password;
        this.repeatedPassword = repeatedPassword;
    }

    /** Returns the requested username. */
    public String getUsername() { return username; }

    /** Returns the entered password. */
    public String getPassword() { return password; }

    /** Returns the confirmation password. */
    public String getRepeatedPassword() { return repeatedPassword; }
}
