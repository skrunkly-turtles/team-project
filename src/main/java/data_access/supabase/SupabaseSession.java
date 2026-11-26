package data_access.supabase;

import java.time.Instant;
import use_case.session.CurrentUserSession;

/** Per-application authenticated identity and automatically refreshed access token. */
public final class SupabaseSession implements CurrentUserSession {
  private final SupabaseAuthClient auth;
  private String username;
  private SupabaseAuthClient.AuthSession credentials;

  public SupabaseSession(SupabaseAuthClient auth) {
    this.auth = auth;
  }

  public synchronized void activate(String name, SupabaseAuthClient.AuthSession tokens) {
    username = name;
    credentials = tokens;
  }

  @Override
  public synchronized void signIn(String name) {
    if (credentials == null || !name.equals(username)) {
      throw new IllegalStateException("Supabase authentication is required before signing in");
    }
  }

  @Override
  public synchronized void signOut() {
    credentials = null;
    username = null;
  }

  @Override
  public synchronized String getCurrentUsername() {
    return username;
  }

  public synchronized String userId() {
    requireSession();
    return credentials.userId();
  }

  public synchronized String email() {
    requireSession();
    return credentials.email();
  }

  /** Existing access token without network traffic; useful for fast GUI logout. */
  public synchronized String existingToken() {
    requireSession();
    return credentials.accessToken();
  }

  public synchronized String token() {
    requireSession();
    if (!Instant.now().plusSeconds(60).isBefore(credentials.expiresAt())) {
      credentials = auth.refresh(credentials.refreshToken());
    }
    return credentials.accessToken();
  }

  private void requireSession() {
    if (credentials == null) {
      throw new IllegalStateException("Not signed in");
    }
  }
}
