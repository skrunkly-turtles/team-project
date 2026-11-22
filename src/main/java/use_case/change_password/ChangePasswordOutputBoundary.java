package use_case.change_password;

/** Receives the outcome of the password-change use case. */
public interface ChangePasswordOutputBoundary {
  /** Presents a successful password change. */
  void prepareSuccessView(ChangePasswordOutputData output);

  /** Presents an unsuccessful password change. */
  void prepareFailView(String message);
}
