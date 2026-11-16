package interface_adapter.change_password;

import interface_adapter.ViewManagerModel;
import use_case.change_password.ChangePasswordInputBoundary;
import use_case.change_password.ChangePasswordInputData;

/** Translates password form values into a change-password use-case request. */
public final class ChangePasswordController {
  private final ChangePasswordInputBoundary interactor;
  private final ViewManagerModel viewManager;

  /** Wires the use case and the profile navigation action. */
  public ChangePasswordController(ChangePasswordInputBoundary interactor,
      ViewManagerModel viewManager) {
    this.interactor = interactor;
    this.viewManager = viewManager;
  }

  /** Requests password replacement after collecting the three form inputs. */
  public void execute(String oldPassword, String newPassword, String repeatedPassword) {
    interactor.execute(new ChangePasswordInputData(oldPassword, newPassword, repeatedPassword));
  }

  /** Returns to the profile after validating the authenticated session. */
  public void cancel() {
    viewManager.show("profile");
  }
}
