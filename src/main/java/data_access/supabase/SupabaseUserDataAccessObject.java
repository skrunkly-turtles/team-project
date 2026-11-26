package data_access.supabase;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Pin;
import models.User;
import data_access.UserStore;

/** Online adapter for current profile and friend-request use cases. */
public final class SupabaseUserDataAccessObject implements UserStore {
  private static final Gson GSON = new Gson();
  private final SupabaseHttp http;
  private final SupabaseSession session;

  public SupabaseUserDataAccessObject(SupabaseHttp http, SupabaseSession session) {
    this.http = http;
    this.session = session;
  }

  private JsonArray list(String path) {
    return http.request("GET", "/rest/v1/" + path, null, session.token()).getAsJsonArray();
  }

  private static String pathValue(String value) {
    return SupabaseHttp.encode(value);
  }

  @Override
  public boolean existsByName(String username) {
    if (username == null) {
      return false;
    }
    return !list("profiles?select=id&username=eq." + pathValue(username)).isEmpty();
  }

  /** Fetches the currently signed-in account's public username by Supabase Auth ID. */
  public String usernameForId(String userId, String temporaryAccessToken) {
    JsonArray result = http.request("GET", "/rest/v1/profiles?select=username&id=eq."
        + pathValue(userId), null, temporaryAccessToken).getAsJsonArray();
    if (result.isEmpty()) {
      throw new IllegalStateException("Profile missing for authenticated account");
    }
    return result.get(0).getAsJsonObject().get("username").getAsString();
  }

  @Override
  public User get(String username) {
    if (username == null) {
      return null;
    }
    JsonArray profiles = list("profiles?select=id,username,visible&username=eq."
        + pathValue(username));
    if (profiles.isEmpty()) {
      return null;
    }
    JsonObject profile = profiles.get(0).getAsJsonObject();
    String id = profile.get("id").getAsString();

    // Only relationship records authorized by RLS are returned.
    Map<String, String> names = profileNames();
    List<String> friends = new ArrayList<>();
    JsonArray edges = list("friendships?select=user_a,user_b&or=(user_a.eq."
        + id + ",user_b.eq." + id + ")");
    for (JsonElement element : edges) {
      JsonObject edge = element.getAsJsonObject();
      String a = edge.get("user_a").getAsString();
      String b = edge.get("user_b").getAsString();
      String friend = names.get(id.equals(a) ? b : a);
      if (friend != null) {
        friends.add(friend);
      }
    }
    List<String> requests = new ArrayList<>();
    JsonArray incoming = list("friend_requests?select=sender&recipient=eq." + id);
    for (JsonElement element : incoming) {
      String from = names.get(element.getAsJsonObject().get("sender").getAsString());
      if (from != null) {
        requests.add(from);
      }
    }
    ArrayList<Pin> pins = new ArrayList<>();
    JsonArray records = list("pins?select=payload&owner=eq." + id);
    for (JsonElement element : records) {
      pins.add(GSON.fromJson(element.getAsJsonObject().get("payload"), Pin.class));
    }
    return new User(username, "supabase-managed-no-local-hash", pins,
        new ArrayList<>(friends), new ArrayList<>(requests),
        profile.get("visible").getAsBoolean());
  }

  /** Returns an independently reconstructed view of all permitted profiles and pins. */
  @Override
  public List<User> getAll() {
    List<User> result = new ArrayList<>();
    for (JsonElement element : list("profiles?select=username&order=username.asc")) {
      String name = element.getAsJsonObject().get("username").getAsString();
      User user = get(name);
      if (user != null) {
        result.add(user);
      }
    }
    return result;
  }

  /** Online signup/password updates happen through Supabase Auth, not raw user records. */
  @Override public void save(User user) {
    throw new UnsupportedOperationException("Use Supabase Auth for signup");
  }
  @Override public void updatePasswordHash(String username, String encodedHash) {
    throw new UnsupportedOperationException("Use Supabase Auth to change passwords");
  }

  /** Reads usernames, never auth email addresses or password hashes. */
  private Map<String, String> profileNames() {
    Map<String, String> names = new HashMap<>();
    for (JsonElement element : list("profiles?select=id,username")) {
      JsonObject object = element.getAsJsonObject();
      names.put(object.get("id").getAsString(), object.get("username").getAsString());
    }
    return names;
  }

  @Override
  public void sendFriendRequest(String username, String friendUsername) {
    requireCurrent(username);
    rpc("send_friend_request", Map.of("target_username", friendUsername));
  }

  /** Accepts a request addressed to the signed-in user. */
  @Override
  public void acceptFriendRequest(String username, String requesterUsername) {
    requireCurrent(username);
    rpc("accept_friend_request", Map.of("sender_username", requesterUsername));
  }

  /** Declines a request addressed to the signed-in user. */
  @Override
  public void declineFriendRequest(String username, String requesterUsername) {
    requireCurrent(username);
    rpc("reject_friend_request", Map.of("sender_username", requesterUsername));
  }

  /** Removes a relationship between the signed-in user and an existing friend. */
  @Override
  public void removeFriend(String username, String friendUsername) {
    requireCurrent(username);
    rpc("remove_friend", Map.of("friend_username", friendUsername));
  }

  // Convenience one-argument versions used by the live integration test.
  public void acceptFriendRequest(String requesterUsername) {
    acceptFriendRequest(session.getCurrentUsername(), requesterUsername);
  }
  public void rejectFriendRequest(String requesterUsername) {
    declineFriendRequest(session.getCurrentUsername(), requesterUsername);
  }
  public void removeFriend(String friendUsername) {
    removeFriend(session.getCurrentUsername(), friendUsername);
  }

  /** Saves the current user's complete pin list atomically in the database. */
  public void updatePins(String username, List<Pin> pins) {
    requireCurrent(username);
    if (pins == null) {
      throw new IllegalArgumentException("Pin list is required");
    }
    rpc("replace_my_pins", Map.of("pin_list", GSON.toJsonTree(pins)));
  }

  /** Creates or updates one pin atomically without replacing other pins. */
  @Override
  public void savePin(String username, Pin pin) {
    requireCurrent(username);
    if (pin == null) {
      throw new IllegalArgumentException("Pin is required");
    }
    rpc("save_my_pin", Map.of("pin_data", GSON.toJsonTree(pin)));
  }

  /** Updates an existing pin owned by the current user. */
  @Override
  public void updatePin(String username, Pin pin) {
    requireCurrent(username);
    if (pin == null) {
      throw new IllegalArgumentException("Pin is required");
    }
    rpc("edit_my_pin", Map.of("pin_data", GSON.toJsonTree(pin)));
  }

  /** Removes an owned pin by ID, without replacing the other saved pins. */
  @Override
  public void deletePin(String username, int pinId) {
    requireCurrent(username);
    rpc("delete_my_pin", Map.of("target_pin_id", pinId));
  }

  /** Updates just the signed-in user's profile visibility setting. */
  public void updateVisibility(String username, boolean visible) {
    requireCurrent(username);
    http.request("PATCH", "/rest/v1/profiles?id=eq." + pathValue(session.userId()),
        Map.of("visible", visible), session.token());
  }

  private void requireCurrent(String username) {
    if (username == null || !username.equals(session.getCurrentUsername())) {
      throw new IllegalStateException("Only the signed-in user's data can be updated");
    }
  }

  private void rpc(String name, Object arguments) {
    http.request("POST", "/rest/v1/rpc/" + name, arguments, session.token());
  }
}
