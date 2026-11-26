package interface_adapter.profile;

import java.util.ArrayList;
import java.util.List;

/** Immutable profile UI state containing only non-sensitive account display data. */
public final class ProfileState {
  private final String username;
  private final String message;
  private final List<String> friends;
  private final List<String> friendRequests;

  /** Creates a profile state with no friends or requests. */
  public ProfileState(String username, String message) {
    this(username, message, new ArrayList<>(), new ArrayList<>());
  }

  /** Creates a profile state with friends but no requests. */
  public ProfileState(String username, String message, List<String> friends) {
    this(username, message, friends, new ArrayList<>());
  }

  /** Creates a profile state with friends and pending incoming requests. */
  public ProfileState(String username, String message, List<String> friends,
                      List<String> friendRequests) {
    this.username = username;
    this.message = message;
    this.friends = new ArrayList<>(friends);
    this.friendRequests = new ArrayList<>(friendRequests);
  }

  /** Returns the active account username, if any. */
  public String getUsername() {
    return username;
  }

  /** Returns the status message shown to the user. */
  public String getMessage() {
    return message;
  }

  /** Returns a copy of the user's friend usernames. */
  public List<String> getFriends() {
    return new ArrayList<>(friends);
  }

  /** Returns a copy of the usernames that have sent the user a friend request. */
  public List<String> getFriendRequests() {
    return new ArrayList<>(friendRequests);
  }
}