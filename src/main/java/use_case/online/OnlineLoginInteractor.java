package use_case.online;

import data_access.supabase.SupabaseAuthClient;
import data_access.supabase.SupabaseSession;
import data_access.supabase.SupabaseUserDataAccessObject;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/** Online login verifies the supplied email/password through Supabase Auth. */
public final class OnlineLoginInteractor implements LoginInputBoundary {
  private final SupabaseAuthClient auth;
  private final SupabaseSession session;
  private final SupabaseUserDataAccessObject accounts;
  private final LoginOutputBoundary presenter;

  public OnlineLoginInteractor(SupabaseAuthClient auth, SupabaseSession session,
      SupabaseUserDataAccessObject accounts, LoginOutputBoundary presenter) {
    this.auth = auth;
    this.session = session;
    this.accounts = accounts;
    this.presenter = presenter;
  }

  @Override
  public void execute(LoginInputData input) {
    if (session.getCurrentUsername() != null) {
      presenter.prepareFailView("Please log out before switching accounts.");
      return;
    }
    if (input.getUsername() == null || input.getPassword() == null) {
      presenter.prepareFailView("Email and password are required.");
      return;
    }
    OnlineUi.background(() -> {
      try {
        var tokens = auth.signIn(input.getUsername(), input.getPassword());
        String username = accounts.usernameForId(tokens.userId(), tokens.accessToken());
        session.activate(username, tokens);
        OnlineUi.present(() -> presenter.prepareSuccessView(new LoginOutputData(username)));
      } catch (RuntimeException exception) {
        session.signOut();
        OnlineUi.present(() -> presenter.prepareFailView(
            "Invalid credentials, unconfirmed email, or server unavailable."));
      }
    });
  }
}
