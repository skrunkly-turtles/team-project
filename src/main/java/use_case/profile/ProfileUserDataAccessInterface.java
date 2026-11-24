package use_case.profile;

import models.User;
/** Narrow account-existence check needed for profile display. */
public interface ProfileUserDataAccessInterface {
  /** Returns whether a registered account still exists. */
  boolean existsByName(String username);
  User get(String username);
}
