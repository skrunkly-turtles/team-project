package interface_adapter.change_password;

/** Immutable status message shown on the change-password screen. */
public final class ChangePasswordState {
  private final String message;

  /** Creates a state with a human-readable message. */
  public ChangePasswordState(String message) {
    this.message = message;
  }

  /** Returns the most recent message. */
  public String getMessage() {
    return message;
  }
}
