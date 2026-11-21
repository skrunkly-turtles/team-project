package use_case.change_password;

/** Defines the application action for changing an authenticated user's password. */
public interface ChangePasswordInputBoundary {
  /** Processes a request to change the currently signed-in user's password. */
  void execute(ChangePasswordInputData input);
}
