package data_access.supabase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Optional live test: runs only when explicit TEST credentials are configured. */
public class SupabaseConnectionTest {
  @Test
  void existingTestUserCanLogInAndReadProfile() {
    String email = System.getenv("SUPABASE_TEST_EMAIL");
    String password = System.getenv("SUPABASE_TEST_PASSWORD");
    String url = System.getenv("SUPABASE_URL");
    String key = System.getenv("SUPABASE_PUBLISHABLE_KEY");
    Assumptions.assumeTrue(email != null && password != null && url != null && key != null,
        "Skip live test without explicit test credentials");
    SupabaseHttp http = new SupabaseHttp(new SupabaseConfig(url, key));
    SupabaseAuthClient auth = new SupabaseAuthClient(http);
    var tokens = auth.signIn(email, password);
    assertNotNull(tokens.accessToken());
    SupabaseSession session = new SupabaseSession(auth);
    SupabaseUserDataAccessObject accounts = new SupabaseUserDataAccessObject(http, session);
    String username = accounts.usernameForId(tokens.userId(), tokens.accessToken());
    session.activate(username, tokens);
    assertEquals(username, accounts.get(username).getUsername());
  }
}
