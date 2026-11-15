package use_case.profile;

/** Narrow account-existence check needed for profile display. */
public interface ProfileUserDataAccessInterface {
  /** Returns whether a registered account still exists. */
  boolean existsByName(String username);
}
