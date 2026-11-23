package interface_adapter.login;

import interface_adapter.ViewModel;

/** Observable state for the login screen. */
public final class LoginViewModel extends ViewModel<LoginState> {
    /** Initializes login with no error message. */
    public LoginViewModel() { super(new LoginState("")); }
}
