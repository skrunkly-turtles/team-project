package interface_adapter.change_password;

import interface_adapter.ViewModel;

/** Observable status state for the account password screen. */
public final class ChangePasswordViewModel extends ViewModel<ChangePasswordState> {
  /** Starts without an error message. */
  public ChangePasswordViewModel() {
    super(new ChangePasswordState(""));
  }
}
