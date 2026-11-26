package interface_adapter.signup;

import interface_adapter.ViewManagerModel;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

/** Converts signup view fields to use-case input. */
public final class SignupController {
  private final SignupInputBoundary interactor;
  private final ViewManagerModel viewManager;

  public SignupController(SignupInputBoundary interactor, ViewManagerModel viewManager) {
    this.interactor = interactor;
    this.viewManager = viewManager;
  }

  /** Original JSON-mode constructor path remains unchanged. */
  public void execute(String username, String password, String repeatedPassword) {
    interactor.execute(new SignupInputData(username, password, repeatedPassword));
  }

  /** Online mode additionally collects a real email for Supabase Auth. */
  public void execute(String username, String email, String password,
      String repeatedPassword) {
    interactor.execute(new SignupInputData(username, email, password, repeatedPassword));
  }

  public void showLogin() { viewManager.show("login"); }
}
