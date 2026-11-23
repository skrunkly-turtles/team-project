package interface_adapter.signup;

import interface_adapter.ViewManagerModel;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

/** Converts signup view inputs into use-case input data. */
public final class SignupController {
    private final SignupInputBoundary interactor;
    private final ViewManagerModel viewManager;

    /** Injects the signup input boundary and navigation model. */
    public SignupController(SignupInputBoundary interactor, ViewManagerModel viewManager) {
        this.interactor = interactor;
        this.viewManager = viewManager;
    }

    /** Executes registration using submitted fields. */
    public void execute(String username, String password, String repeatedPassword) {
        interactor.execute(new SignupInputData(username, password, repeatedPassword));
    }

    /** Navigates to login without attempting registration. */
    public void showLogin() { viewManager.show("login"); }
}
