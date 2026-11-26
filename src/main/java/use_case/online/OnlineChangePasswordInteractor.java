package use_case.online;

import data_access.supabase.SupabaseAuthClient;
import data_access.supabase.SupabaseSession;
import use_case.change_password.ChangePasswordInputBoundary;
import use_case.change_password.ChangePasswordInputData;
import use_case.change_password.ChangePasswordOutputBoundary;
import use_case.change_password.ChangePasswordOutputData;

/** Requires the current password and updates credentials in Supabase Auth. */
public final class OnlineChangePasswordInteractor implements ChangePasswordInputBoundary {
  private final SupabaseAuthClient auth;
  private final SupabaseSession session;
  private final ChangePasswordOutputBoundary presenter;

  public OnlineChangePasswordInteractor(SupabaseAuthClient auth, SupabaseSession session,
      ChangePasswordOutputBoundary presenter) {
    this.auth = auth;
    this.session = session;
    this.presenter = presenter;
  }

  @Override
  public void execute(ChangePasswordInputData input) {
    String username = session.getCurrentUsername();
    if (username == null) {
      presenter.prepareFailView("Please sign in first.");
    } else if (input.getCurrentPassword() == null
        || input.getNewPassword() == null || input.getNewPassword().length() < 8) {
      presenter.prepareFailView("Enter your current password and a new password of 8+ characters.");
    } else if (!input.getNewPassword().equals(input.getRepeatedPassword())) {
      presenter.prepareFailView("New passwords do not match.");
    } else if (input.getNewPassword().equals(input.getCurrentPassword())) {
      presenter.prepareFailView("New password must differ from the old password.");
    } else {
      OnlineUi.background(() -> {
        try {
          String email = session.email();
          // Verify the old password; do not trust only a locally cached login.
          var validated = auth.signIn(email, input.getCurrentPassword());
          auth.changePassword(validated.accessToken(), input.getNewPassword());
          // Obtain a fresh session for subsequent online calls.
          var renewed = auth.signIn(email, input.getNewPassword());
          session.activate(username, renewed);
          OnlineUi.present(() -> presenter.prepareSuccessView(
              new ChangePasswordOutputData(username)));
        } catch (RuntimeException exception) {
          OnlineUi.present(() -> presenter.prepareFailView(
              "Could not change password. Check the old password and connection."));
        }
      });
    }
  }
}
