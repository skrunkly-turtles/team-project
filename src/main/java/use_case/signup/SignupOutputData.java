package use_case.signup;

/** Non-sensitive result of successful account creation. */
public final class SignupOutputData {
    private final String username;

    /** Creates an output value for the newly registered username. */
    public SignupOutputData(String username) { this.username = username; }

    /** Returns the registered username. */
    public String getUsername() { return username; }
}
