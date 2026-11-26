package data_access.supabase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.Map;

/** Wraps Supabase Auth. Passwords are sent only to Supabase over HTTPS. */
public final class SupabaseAuthClient {
  private final SupabaseHttp http;

  public SupabaseAuthClient(SupabaseHttp http) {
    this.http = http;
  }

  public void signUp(String username, String email, String password) {
    http.request("POST", "/auth/v1/signup", Map.of(
        "email", email.trim(), "password", password,
        "data", Map.of("username", username)), null);
  }

  public AuthSession signIn(String email, String password) {
    JsonElement result = http.request("POST", "/auth/v1/token?grant_type=password",
        Map.of("email", email.trim(), "password", password), null);
    return AuthSession.from(result.getAsJsonObject());
  }

  public AuthSession refresh(String refreshToken) {
    JsonElement result = http.request("POST", "/auth/v1/token?grant_type=refresh_token",
        Map.of("refresh_token", refreshToken), null);
    return AuthSession.from(result.getAsJsonObject());
  }

  public void changePassword(String accessToken, String newPassword) {
    http.request("PUT", "/auth/v1/user", Map.of("password", newPassword), accessToken);
  }

  public void signOut(String accessToken) {
    http.request("POST", "/auth/v1/logout", null, accessToken);
  }

  /** Supabase-issued tokens and identity. Never expose these through a ViewModel. */
  public record AuthSession(String userId, String email, String accessToken,
      String refreshToken, Instant expiresAt) {
    private static AuthSession from(JsonObject data) {
      JsonObject user = data.getAsJsonObject("user");
      if (user == null || !data.has("access_token") || !data.has("refresh_token")) {
        throw new IllegalStateException("Supabase did not return an active session");
      }
      long seconds = data.has("expires_in") ? data.get("expires_in").getAsLong() : 3600;
      return new AuthSession(user.get("id").getAsString(), user.get("email").getAsString(),
          data.get("access_token").getAsString(),
          data.get("refresh_token").getAsString(), Instant.now().plusSeconds(seconds));
    }
  }
}
