package use_case.online;

import data_access.supabase.SupabaseAuthClient;
import data_access.supabase.SupabaseSession;
import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutOutputBoundary;

/** Clears local session immediately and asks Supabase to revoke server-side session. */
public final class OnlineLogoutInteractor implements LogoutInputBoundary {
  private final SupabaseAuthClient auth;
  private final SupabaseSession session;
  private final LogoutOutputBoundary presenter;

  public OnlineLogoutInteractor(SupabaseAuthClient auth, SupabaseSession session,
      LogoutOutputBoundary presenter) {
    this.auth = auth;
    this.session = session;
    this.presenter = presenter;
  }

  @Override
  public void execute() {
    String token = null;
    if (session.getCurrentUsername() != null) {
      try {
        token = session.existingToken();
      } catch (RuntimeException ignored) {
        // Still clear local state, even when connectivity is lost.
      }
    }
    session.signOut();
    presenter.prepareSuccessView();
    if (token != null) {
      String previousToken = token;
      OnlineUi.background(() -> {
        try {
          auth.signOut(previousToken);
        } catch (RuntimeException ignored) {
          // Local sign-out succeeded; remote revocation may fail if offline.
        }
      });
    }
  }
}
