package use_case.profile;

/** Profile display data; excludes password hashes and private account storage details. */
public final class ProfileOutputData {
  private final String username;

  /** Constructs safe-to-display profile data. */
  public ProfileOutputData(String username) {
    this.username = username;
  }

  /** Returns the public username shown on the profile. */
  public String getUsername() {
    return username;
  }
}
