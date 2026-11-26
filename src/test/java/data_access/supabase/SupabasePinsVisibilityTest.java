package data_access.supabase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import models.Pin;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Live two-account pin visibility/edit/delete check; use dedicated test users only. */
public class SupabasePinsVisibilityTest {
  @Test
  void publicPinBecomesPrivateAndCannotBeEditedByOtherUser() {
    String url = System.getenv("SUPABASE_URL");
    String key = System.getenv("SUPABASE_PUBLISHABLE_KEY");
    String aEmail = System.getenv("SUPABASE_TEST_EMAIL");
    String aPass = System.getenv("SUPABASE_TEST_PASSWORD");
    String bEmail = System.getenv("SUPABASE_TEST_EMAIL_B");
    String bPass = System.getenv("SUPABASE_TEST_PASSWORD_B");
    Assumptions.assumeTrue(url != null && key != null && aEmail != null
        && aPass != null && bEmail != null && bPass != null,
        "Live tests require two dedicated test accounts");
    SupabaseHttp http = new SupabaseHttp(new SupabaseConfig(url, key));
    SupabaseAuthClient auth = new SupabaseAuthClient(http);
    var aAuth = auth.signIn(aEmail, aPass);
    var bAuth = auth.signIn(bEmail, bPass);
    SupabaseSession aSession = new SupabaseSession(auth);
    SupabaseSession bSession = new SupabaseSession(auth);
    SupabaseUserDataAccessObject a = new SupabaseUserDataAccessObject(http, aSession);
    SupabaseUserDataAccessObject b = new SupabaseUserDataAccessObject(http, bSession);
    String alice = a.usernameForId(aAuth.userId(), aAuth.accessToken());
    String bob = b.usernameForId(bAuth.userId(), bAuth.accessToken());
    aSession.activate(alice, aAuth);
    bSession.activate(bob, bAuth);

    int pinId = new SecureRandom().nextInt(1, Integer.MAX_VALUE);
    String marker = "online-pin-smoke-" + pinId;
    Pin publicPin = new Pin(pinId, alice, null, null, marker + " public", true);
    Pin privatePin = new Pin(pinId, alice, null, null, marker + " private", false);
    try {
      a.savePin(alice, publicPin);
      assertTrue(b.get(alice).getPins().stream()
          .anyMatch(pin -> pin.getDescription().equals(marker + " public")));
      a.updatePin(alice, privatePin);
      assertTrue(a.get(alice).getPins().stream()
          .anyMatch(pin -> pin.getDescription().equals(marker + " private")));
      // If these two throwaway users happen to be friends, friends-only visibility
      // would differ, but private pins must remain private regardless.
      assertFalse(b.get(alice).getPins().stream()
          .anyMatch(pin -> pin.getDescription().equals(marker + " private")));
      assertThrows(IllegalStateException.class, () -> b.deletePin(alice, pinId));
      assertTrue(a.get(alice).getPins().stream()
          .anyMatch(pin -> pin.getDescription().equals(marker + " private")));
    } finally {
      a.deletePin(alice, pinId);
    }
  }
}
