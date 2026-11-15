package use_case.login;

import models.User;
import use_case.security.PasswordHasher;
import use_case.session.CurrentUserSession;

/** Authenticates credentials without exposing stored password hashes to the UI. */
public final class LoginInteractor implements LoginInputBoundary {
    private final LoginUserDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;
    private final PasswordHasher passwordHasher;
    private final CurrentUserSession session;

    /** Injects account lookup, presenter, hash verifier and current session. */
    public LoginInteractor(LoginUserDataAccessInterface userDataAccess,
                           LoginOutputBoundary presenter, PasswordHasher passwordHasher,
                           CurrentUserSession session) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
        this.passwordHasher = passwordHasher;
        this.session = session;
    }

    /** Authenticates the user or reports a generic error. */
    @Override
    public void execute(LoginInputData input) {
        if (session.getCurrentUsername() != null) {
            presenter.prepareFailView("Please log out before switching accounts.");
            return;
        }
        String username = input.getUsername();
        User user = username == null ? null : userDataAccess.get(username);
        if (user == null || input.getPassword() == null
                || !passwordHasher.matches(input.getPassword(), user.getPasswordHash())) {
            presenter.prepareFailView("Invalid username or password.");
            return;
        }
        session.signIn(user.getUsername());
        presenter.prepareSuccessView(new LoginOutputData(user.getUsername()));
    }
}
