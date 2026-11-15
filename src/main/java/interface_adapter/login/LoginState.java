package interface_adapter.login;

/** Immutable login-screen state excluding the typed password. */
public final class LoginState {
    private final String message;

    /** Stores a user-visible status or error message. */
    public LoginState(String message) { this.message = message; }

    /** Returns the current message. */
    public String getMessage() { return message; }
}
