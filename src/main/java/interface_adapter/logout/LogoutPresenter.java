package interface_adapter.logout;

import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordState;
import interface_adapter.change_password.ChangePasswordViewModel;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import use_case.logout.LogoutOutputBoundary;

/** Clears authenticated UI state and redirects the application to the login screen. */
public final class LogoutPresenter implements LogoutOutputBoundary {
  private final LoginViewModel login;
  private final HomeViewModel home;
  private final ProfileViewModel profile;
  private final ChangePasswordViewModel password;
  private final ViewManagerModel viewManager;

  /** Injects all account-sensitive state holders that must be cleared on logout. */
  public LogoutPresenter(LoginViewModel login, HomeViewModel home,
      ProfileViewModel profile, ChangePasswordViewModel password,
      ViewManagerModel viewManager) {
    this.login = login;
    this.home = home;
    this.profile = profile;
    this.password = password;
    this.viewManager = viewManager;
  }

  /** Clears all user-specific view models before showing the login form. */
  @Override
  public void prepareSuccessView() {
    home.setState(new HomeState(null));
    profile.setState(new ProfileState(null, ""));
    password.setState(new ChangePasswordState(""));
    login.setState(new LoginState("You have logged out."));
    viewManager.show("login");
  }
}
