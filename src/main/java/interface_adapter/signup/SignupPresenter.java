package interface_adapter.signup;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/** Converts signup use-case results into view-model updates. */
public final class SignupPresenter implements SignupOutputBoundary {
    private final SignupViewModel signupViewModel;
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManager;

    /** Injects the view models affected by signup. */
    public SignupPresenter(SignupViewModel signupViewModel, LoginViewModel loginViewModel,
                           ViewManagerModel viewManager) {
        this.signupViewModel = signupViewModel;
        this.loginViewModel = loginViewModel;
        this.viewManager = viewManager;
    }

    /** Shows confirmation on the login screen. */
    @Override
    public void prepareSuccessView(SignupOutputData result) {
        signupViewModel.setState(new SignupState(""));
        loginViewModel.setState(new LoginState("Account created for " + result.getUsername()
                + ". Please log in."));
        viewManager.show("login");
    }

    /** Displays signup error without navigating away. */
    @Override
    public void prepareFailView(String errorMessage) {
        signupViewModel.setState(new SignupState(errorMessage));
    }
}
