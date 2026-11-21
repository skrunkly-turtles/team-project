package interface_adapter.profile;

import interface_adapter.ViewManagerModel;
import use_case.profile.ProfileInputBoundary;

/** Handles requests to open the authenticated user's profile. */
public final class ProfileController {
  private final ProfileInputBoundary interactor;
  private final ViewManagerModel viewManager;

  /** Connects the request boundary and navigation model. */
  public ProfileController(ProfileInputBoundary interactor, ViewManagerModel viewManager) {
    this.interactor = interactor;
    this.viewManager = viewManager;
  }

  /** Requests the current user's profile. */
  public void execute() {
    interactor.execute();
  }

  /** Returns to the authenticated application's temporary home screen. */
  public void showHome() {
    viewManager.show("home");
  }

  /** Opens the password-change screen. */
  public void showChangePassword() {
    viewManager.show("change-password");
  }
}
