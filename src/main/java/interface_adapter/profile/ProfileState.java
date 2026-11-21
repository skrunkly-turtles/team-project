package interface_adapter.profile;

/** Immutable profile UI state containing only non-sensitive account display data. */
public final class ProfileState {
  private final String username;
  private final String message;

  /** Creates a profile state. */
  public ProfileState(String username, String message) {
    this.username = username;
    this.message = message;
  }

  /** Returns the active account username, if any. */
  public String getUsername() {
    return username;
  }

  /** Returns the status message shown to the user. */
  public String getMessage() {
    return message;
  }
}
