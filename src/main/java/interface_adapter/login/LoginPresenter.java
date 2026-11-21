package interface_adapter.login;

import interface_adapter.ViewManagerModel;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/** Updates login and authenticated home view models after authentication. */
public final class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel loginViewModel;
    private final HomeViewModel homeViewModel;
    private final ViewManagerModel viewManager;

    /** Injects the view models for navigation on success or failure. */
    public LoginPresenter(LoginViewModel loginViewModel, HomeViewModel homeViewModel,
                          ViewManagerModel viewManager) {
        this.loginViewModel = loginViewModel;
        this.homeViewModel = homeViewModel;
        this.viewManager = viewManager;
    }

    /** Shows the authenticated home screen. */
    @Override
    public void prepareSuccessView(LoginOutputData outputData) {
        loginViewModel.setState(new LoginState(""));
        homeViewModel.setState(new HomeState(outputData.getUsername()));
        viewManager.show("home");
    }

    /** Displays a generic authentication error. */
    @Override
    public void prepareFailView(String errorMessage) {
        loginViewModel.setState(new LoginState(errorMessage));
    }
}
