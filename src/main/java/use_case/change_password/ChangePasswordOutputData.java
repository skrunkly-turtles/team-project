package use_case.change_password;

/** The non-sensitive result of a successful password change. */
public final class ChangePasswordOutputData {
  private final String username;

  /** Records which signed-in user changed their password. */
  public ChangePasswordOutputData(String username) {
    this.username = username;
  }

  /** Returns the account's public username. */
  public String getUsername() {
    return username;
  }
}
