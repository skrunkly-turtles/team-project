package use_case.profile;

import java.util.ArrayList;
import java.util.List;

/** Profile display data; excludes password hashes and private account storage details. */
public final class ProfileOutputData {
  private final String username;
  private final List<String> friends;
  private final List<String> friendRequests;

  /** Constructs profile data with no friends. */
  public ProfileOutputData(String username) {
    this(username, new ArrayList<>(), new ArrayList<>());
  }

  /** Constructs safe-to-display profile data including the user's friends. */
  public ProfileOutputData(String username, List<String> friends,  List<String> friendRequests) {
    this.username = username;
    this.friends = new ArrayList<>(friends);
    this.friendRequests = new ArrayList<>(friendRequests);
  }

  /** Returns the public username shown on the profile. */
  public String getUsername() {
    return username;
  }

  /** Returns a copy of the friend usernames. */
  public List<String> getFriends() {
    return new ArrayList<>(friends);
  }

  public List<String> getFriendRequests() {
    return new ArrayList<>(friendRequests);
  }
}