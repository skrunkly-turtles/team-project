package interface_adapter.login;

import interface_adapter.ViewManagerModel;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;

/** Converts login view inputs into the authentication use case. */
public final class LoginController {
    private final LoginInputBoundary interactor;
    private final ViewManagerModel viewManager;

    /** Injects the authentication use case and navigation model. */
    public LoginController(LoginInputBoundary interactor, ViewManagerModel viewManager) {
        this.interactor = interactor;
        this.viewManager = viewManager;
    }

    /** Performs a login attempt. */
    public void execute(String username, String password) {
        interactor.execute(new LoginInputData(username, password));
    }

    /** Navigates to the signup screen. */
    public void showSignup() { viewManager.show("signup"); }
}
