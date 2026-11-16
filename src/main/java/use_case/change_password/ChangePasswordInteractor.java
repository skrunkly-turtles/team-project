package use_case.change_password;

import models.User;
import use_case.security.PasswordHasher;
import use_case.session.CurrentUserSession;

/** Implements current-password verification and secure replacement of account credentials. */
public final class ChangePasswordInteractor implements ChangePasswordInputBoundary {
  private static final int MIN_PASSWORD_LENGTH = 8;
  private final ChangePasswordUserDataAccessInterface accounts;
  private final ChangePasswordOutputBoundary presenter;
  private final PasswordHasher hasher;
  private final CurrentUserSession session;

  /** Creates the interactor with only inward-facing interfaces. */
  public ChangePasswordInteractor(ChangePasswordUserDataAccessInterface accounts,
      ChangePasswordOutputBoundary presenter, PasswordHasher hasher,
      CurrentUserSession session) {
    this.accounts = accounts;
    this.presenter = presenter;
    this.hasher = hasher;
    this.session = session;
  }

  /** Verifies the old password, validates the replacement and persists its hash. */
  @Override
  public void execute(ChangePasswordInputData input) {
    String username = session.getCurrentUsername();
    if (username == null) {
      presenter.prepareFailView("Please sign in to change your password.");
      return;
    }
    User current = accounts.get(username);
    if (current == null) {
      presenter.prepareFailView("Your account could not be found.");
      return;
    }
    if (input.getCurrentPassword() == null
        || !hasher.matches(input.getCurrentPassword(), current.getPasswordHash())) {
      presenter.prepareFailView("Current password is incorrect.");
      return;
    }
    String replacement = input.getNewPassword();
    if (replacement == null || replacement.length() < MIN_PASSWORD_LENGTH) {
      presenter.prepareFailView("New password must contain at least 8 characters.");
      return;
    }
    if (!replacement.equals(input.getRepeatedPassword())) {
      presenter.prepareFailView("New passwords do not match.");
      return;
    }
    if (hasher.matches(replacement, current.getPasswordHash())) {
      presenter.prepareFailView("New password must be different from the old password.");
      return;
    }
    try {
      accounts.updatePasswordHash(username, hasher.hash(replacement));
      presenter.prepareSuccessView(new ChangePasswordOutputData(username));
    } catch (IllegalStateException exception) {
      presenter.prepareFailView("Could not update password. Please try again.");
    }
  }
}
