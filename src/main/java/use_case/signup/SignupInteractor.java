package use_case.signup;

import java.util.regex.Pattern;
import models.User;
import models.UserFactory;
import models.CommonUserFactory;
import use_case.security.PasswordHasher;

/** Implements the business rules for creating a new user account. */
public final class SignupInteractor implements SignupInputBoundary {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z][A-Za-z0-9_]{2,23}");
    private final SignupUserDataAccessInterface userDataAccess;
    private final SignupOutputBoundary presenter;
    private final UserFactory userFactory;
    private final PasswordHasher passwordHasher;

    /** Injects storage, output, factory, and cryptography abstractions. */
    public SignupInteractor(SignupUserDataAccessInterface userDataAccess,
                            SignupOutputBoundary presenter, CommonUserFactory userFactory,
                            PasswordHasher passwordHasher) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
        this.userFactory = userFactory;
        this.passwordHasher = passwordHasher;
    }

    /** Validates credentials and creates a new account when possible. */
    @Override
    public void execute(SignupInputData input) {
        String username = input.getUsername();
        String password = input.getPassword();
        String repeated = input.getRepeatedPassword();
        if (username == null || !USERNAME.matcher(username).matches()) {
            presenter.prepareFailView("Username must be 3-24 characters, start with a letter, and use letters, numbers or _ only.");
        } else if (password == null || password.length() < 8) {
            presenter.prepareFailView("Password must contain at least 8 characters.");
        } else if (!password.equals(repeated)) {
            presenter.prepareFailView("Passwords do not match.");
        } else if (userDataAccess.existsByName(username)) {
            presenter.prepareFailView("Username already exists.");
        } else {
            String encodedHash = passwordHasher.hash(password);
            User user = userFactory.create(username, encodedHash);
            try {
                userDataAccess.save(user);
                presenter.prepareSuccessView(new SignupOutputData(username));
            } catch (IllegalStateException e) {
                presenter.prepareFailView("Could not save account: " + e.getMessage());
            }
        }
    }
}
