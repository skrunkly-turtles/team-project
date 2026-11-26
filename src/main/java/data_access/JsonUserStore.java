package data_access;

import java.util.List;
import java.util.ArrayList;
import models.Pin;
import models.User;

/** Simple wrapper retaining the existing JSON data-access object unchanged. */
public final class JsonUserStore implements UserStore {
  private final JsonUserDataAccessObject dao;

  public JsonUserStore(JsonUserDataAccessObject dao) { this.dao = dao; }
  @Override public boolean existsByName(String username) { return dao.existsByName(username); }
  @Override public User get(String username) { return dao.get(username); }
  @Override public void save(User user) { dao.save(user); }
  @Override public void updatePasswordHash(String username, String hash) {
    dao.updatePasswordHash(username, hash);
  }
  @Override public void sendFriendRequest(String username, String friendUsername) {
    dao.sendFriendRequest(username, friendUsername);
  }
  @Override public void updatePins(String username, List<Pin> pins) { dao.updatePins(username, pins); }
  @Override public void savePin(String username, Pin pin) {
    if (pin == null) throw new IllegalArgumentException("Pin is required");
    User user = dao.get(username);
    if (user == null) throw new IllegalStateException("Account does not exist");
    List<Pin> pins = new ArrayList<>(user.getPins());
    pins.add(pin);
    dao.updatePins(username, pins);
  }
  @Override public void updateVisibility(String username, boolean visible) {
    dao.updateVisibility(username, visible);
  }

  // The following use cases are on open PRs, not on this pinned main revision.
  // Replace this file with extras/after-merge/JsonUserStore.java after those PRs merge.
  @Override public List<User> getAll() { throw unavailable(); }
  @Override public void updatePin(String username, Pin pin) { throw unavailable(); }
  @Override public void deletePin(String username, int pinId) { throw unavailable(); }
  @Override public void acceptFriendRequest(String username, String requesterUsername) { throw unavailable(); }
  @Override public void declineFriendRequest(String username, String requesterUsername) { throw unavailable(); }
  @Override public void removeFriend(String username, String friendUsername) { throw unavailable(); }
  private static UnsupportedOperationException unavailable() {
    return new UnsupportedOperationException("This use case has not merged into main yet");
  }
}
