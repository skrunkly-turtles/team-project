package use_case.profile;

/** Receives the current profile's non-sensitive display data. */
public interface ProfileOutputBoundary {
  /** Presents the signed-in user's profile. */
  void prepareSuccessView(ProfileOutputData output);

  /** Routes unauthorized or invalid profile requests to an error view. */
  void prepareFailView(String message);
}
