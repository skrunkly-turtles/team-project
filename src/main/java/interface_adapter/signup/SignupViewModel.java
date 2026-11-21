package interface_adapter.signup;

import interface_adapter.ViewModel;

/** Observable state for the signup screen. */
public final class SignupViewModel extends ViewModel<SignupState> {
    /** Initializes signup with no error message. */
    public SignupViewModel() { super(new SignupState("")); }
}
