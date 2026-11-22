package interface_adapter.profile;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import use_case.profile.ProfileOutputBoundary;
import use_case.profile.ProfileOutputData;

/** Updates profile state and navigates according to authentication status. */
public final class ProfilePresenter implements ProfileOutputBoundary {
  private final ProfileViewModel profileViewModel;
  private final LoginViewModel loginViewModel;
  private final ViewManagerModel viewManager;

  /** Creates a presenter with the relevant output view models. */
  public ProfilePresenter(ProfileViewModel profileViewModel,
      LoginViewModel loginViewModel, ViewManagerModel viewManager) {
    this.profileViewModel = profileViewModel;
    this.loginViewModel = loginViewModel;
    this.viewManager = viewManager;
  }

  /** Displays the signed-in user's general profile. */
  @Override
  public void prepareSuccessView(ProfileOutputData output) {
    profileViewModel.setState(new ProfileState(output.getUsername(), ""));
    viewManager.show("profile");
  }

  /** Sends unauthenticated users to the login screen. */
  @Override
  public void prepareFailView(String message) {
    profileViewModel.setState(new ProfileState(null, ""));
    loginViewModel.setState(new LoginState(message));
    viewManager.show("login");
  }
}
