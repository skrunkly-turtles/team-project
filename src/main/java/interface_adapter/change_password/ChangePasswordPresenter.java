package interface_adapter.change_password;

import interface_adapter.ViewManagerModel;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import use_case.change_password.ChangePasswordOutputBoundary;
import use_case.change_password.ChangePasswordOutputData;

/** Converts password-change outcomes into messages for Swing views. */
public final class ChangePasswordPresenter implements ChangePasswordOutputBoundary {
  private final ChangePasswordViewModel changePasswordViewModel;
  private final ProfileViewModel profileViewModel;
  private final ViewManagerModel viewManager;

  /** Connects state holders and the screen navigator. */
  public ChangePasswordPresenter(ChangePasswordViewModel changePasswordViewModel,
      ProfileViewModel profileViewModel, ViewManagerModel viewManager) {
    this.changePasswordViewModel = changePasswordViewModel;
    this.profileViewModel = profileViewModel;
    this.viewManager = viewManager;
  }

  /** Returns to the profile with an account-success message. */
  @Override
  public void prepareSuccessView(ChangePasswordOutputData output) {
    changePasswordViewModel.setState(new ChangePasswordState(""));
    profileViewModel.setState(new ProfileState(output.getUsername(),
        "Your password was changed successfully."));
    viewManager.show("profile");
  }

  /** Keeps the form visible and shows the validation error. */
  @Override
  public void prepareFailView(String message) {
    changePasswordViewModel.setState(new ChangePasswordState(message));
  }
}
