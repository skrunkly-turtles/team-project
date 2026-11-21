package use_case.profile;

/** Defines retrieval of a profile belonging to the currently signed-in user. */
public interface ProfileInputBoundary {
  /** Loads the current user's profile details. */
  void execute();
}
