package use_case.change_password;

/** Password-change form data passed from the controller to the interactor. */
public final class ChangePasswordInputData {
  private final String currentPassword;
  private final String newPassword;
  private final String repeatedPassword;

  /** Creates a request without including an untrusted username. */
  public ChangePasswordInputData(String currentPassword, String newPassword,
      String repeatedPassword) {
    this.currentPassword = currentPassword;
    this.newPassword = newPassword;
    this.repeatedPassword = repeatedPassword;
  }

  /** Returns the password supplied to prove account ownership. */
  public String getCurrentPassword() {
    return currentPassword;
  }

  /** Returns the requested replacement password. */
  public String getNewPassword() {
    return newPassword;
  }

  /** Returns the new password confirmation. */
  public String getRepeatedPassword() {
    return repeatedPassword;
  }
}
