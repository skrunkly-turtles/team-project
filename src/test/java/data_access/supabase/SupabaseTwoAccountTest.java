package data_access.supabase;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Optional live test; use ONLY dedicated throwaway accounts because it changes friendships. */
public class SupabaseTwoAccountTest {
  @Test
  void friendRequestAcrossTwoAccounts() {
    String url = System.getenv("SUPABASE_URL");
    String key = System.getenv("SUPABASE_PUBLISHABLE_KEY");
    String aEmail = System.getenv("SUPABASE_TEST_EMAIL");
    String aPassword = System.getenv("SUPABASE_TEST_PASSWORD");
    String bEmail = System.getenv("SUPABASE_TEST_EMAIL_B");
    String bPassword = System.getenv("SUPABASE_TEST_PASSWORD_B");
    Assumptions.assumeTrue(url != null && key != null && aEmail != null
        && aPassword != null && bEmail != null && bPassword != null,
        "Skip live integration test without two explicit TEST accounts");

    SupabaseHttp http = new SupabaseHttp(new SupabaseConfig(url, key));
    SupabaseAuthClient auth = new SupabaseAuthClient(http);
    var aCredentials = auth.signIn(aEmail, aPassword);
    var bCredentials = auth.signIn(bEmail, bPassword);
    SupabaseSession aSession = new SupabaseSession(auth);
    SupabaseSession bSession = new SupabaseSession(auth);
    SupabaseUserDataAccessObject aliceDao = new SupabaseUserDataAccessObject(http, aSession);
    SupabaseUserDataAccessObject bobDao = new SupabaseUserDataAccessObject(http, bSession);
    String alice = aliceDao.usernameForId(aCredentials.userId(), aCredentials.accessToken());
    String bob = bobDao.usernameForId(bCredentials.userId(), bCredentials.accessToken());
    aSession.activate(alice, aCredentials);
    bSession.activate(bob, bCredentials);

    // Idempotent after first run; do not modify real teammates' accounts.
    if (!aliceDao.get(alice).getFriends().contains(bob)) {
      if (!bobDao.get(bob).getFriendRequests().contains(alice)) {
        aliceDao.sendFriendRequest(alice, bob);
      }
      assertTrue(bobDao.get(bob).getFriendRequests().contains(alice));
      bobDao.acceptFriendRequest(alice);
    }
    assertTrue(aliceDao.get(alice).getFriends().contains(bob));
    assertTrue(bobDao.get(bob).getFriends().contains(alice));
    aliceDao.removeFriend(bob);
    assertFalse(aliceDao.get(alice).getFriends().contains(bob));
    assertFalse(bobDao.get(bob).getFriends().contains(alice));
  }
}
