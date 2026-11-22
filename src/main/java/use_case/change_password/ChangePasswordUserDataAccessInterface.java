package use_case.change_password;

import models.User;

/** Storage operations required by the change-password use case. */
public interface ChangePasswordUserDataAccessInterface {
  /** Returns the account associated with a username, or null if none exists. */
  User get(String username);

  /** Updates the stored password hash for one registered account. */
  void updatePasswordHash(String username, String encodedHash);
}
