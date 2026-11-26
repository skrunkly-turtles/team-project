package use_case.signup;

/** Input collected from the signup screen. Email is only needed in online mode. */
public final class SignupInputData {
  private final String username;
  private final String email;
  private final String password;
  private final String repeatedPassword;

  public SignupInputData(String username, String password, String repeatedPassword) {
    this(username, null, password, repeatedPassword);
  }

  public SignupInputData(String username, String email, String password,
      String repeatedPassword) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.repeatedPassword = repeatedPassword;
  }

  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }
  public String getRepeatedPassword() { return repeatedPassword; }
}
