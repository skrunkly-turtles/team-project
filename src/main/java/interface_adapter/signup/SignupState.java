package interface_adapter.signup;

/** Immutable signup view state, excluding secret credentials. */
public final class SignupState {
    private final String message;

    /** Stores a user-visible status or error message. */
    public SignupState(String message) { this.message = message; }

    /** Returns status/error text. */
    public String getMessage() { return message; }
}
