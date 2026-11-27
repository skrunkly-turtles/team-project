package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A registered application user, preserving the team's original account/pin/friend model.
 *
 * <p>The {@code passwordHash} field contains a one-way password hash. See Pbkdf2PasswordHasher
 * for more details.
 * Pin and friendship use cases belong to the other team members.
 */
public final class User {
  private final String username;
  private final String passwordHash;
  private final ArrayList<Pin> pins;
  private final ArrayList<String> friends;
  private final ArrayList<String> friendRequests;
  private boolean visible;

  /**
   * Constructs a user with the original five-argument constructor signature.
   *
   * @param username unique account name
   * @param passwordHash stored hash rather than the plaintext password
   * @param pins current user's pins
   * @param friends current user's friend usernames
   * @param visible existing profile visibility setting
   */
  public User(String username, String passwordHash, ArrayList<Pin> pins,
      ArrayList<String> friends, ArrayList<String> friendRequests, boolean visible) {
    this.username = Objects.requireNonNull(username, "username");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.pins = new ArrayList<>(Objects.requireNonNull(pins, "pins"));
    this.friends = new ArrayList<>(Objects.requireNonNull(friends, "friends"));
    this.friendRequests = new ArrayList<>(Objects.requireNonNull(friendRequests, "pendingFriends"));
    this.visible = visible;
  }

  /**
   * Constructs an account using the team's original four-argument signature.
   *
   * @param username unique account name
   * @param passwordHash stored password hash
   * @param pins user's pins
   * @param friends usernames of friends
   */
  public User(String username, String passwordHash, ArrayList<Pin> pins,
      ArrayList<String> friends) {
    this(username, passwordHash, pins, friends, new ArrayList<>(), false);
  }

  /** Returns the user's unique name. */
  public String getUsername() {
    return username;
  }

  /** Returns the stored hash for authentication use cases only. */
  public String getPasswordHash() {
    return passwordHash;
  }

  /** Returns a defensive copy of the pin list. */
  public List<Pin> getPins() {
    return new ArrayList<>(pins);
  }

  /** Returns a defensive copy of the friend list. */
  public List<String> getFriends() {
    return new ArrayList<>(friends);
  }

  /** Returns a defensive copy of the pendingFriends list. */
  public List<String> getFriendRequests() {
    return new ArrayList<>(friendRequests);
  }

  /** Records an incoming request, ignoring duplicates. */
  public void receiveFriendRequest(String fromUsername) {
    if (fromUsername.isEmpty() || username.equals(fromUsername)) {
      throw new IllegalArgumentException("Illegal username input");
    }
    if (!friendRequests.contains(fromUsername)) {
      friendRequests.add(fromUsername);
    }
  }

  /** Removes an incoming request if present; does nothing otherwise. */
  public void removeFriendRequest(String fromUsername) {
    friendRequests.remove(fromUsername);
  }

  /**
   * Accepts a pending request: clears it and adds the sender as a friend.
   *
   * @param fromUsername the user who sent the request
   * @throws IllegalArgumentException if there is no pending request from that user
   *     or they are already a friend
   */
  public void acceptFriendRequest(String fromUsername) {
    if (!friendRequests.contains(fromUsername)) {
      throw new IllegalArgumentException("No pending request");
    }
    if (friends.contains(fromUsername)) {
      throw new IllegalArgumentException("Friend already exists");
    }
    friendRequests.remove(fromUsername);
    addFriend(fromUsername);
  }

  /**
   * Declines a pending request by removing it, without creating a friendship.
   *
   * @param fromUsername the user who sent the request
   * @throws IllegalArgumentException if there is no pending request from that user
   */
  public void declineFriendRequest(String fromUsername) {
    if (!friendRequests.remove(fromUsername)) {
      throw new IllegalArgumentException("No pending request");
    }
  }

  /** Returns the existing profile visibility setting. */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Sets the existing visibility flag; persistence is handled by the owner's DAO.
   *
   * @param visible the updated visibility flag
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Adds another User as a friend, if they do not exist as a friend already
   *
   * @param friendUsername is the String name of the other User
   */
  public void addFriend(String friendUsername) {
    if (friendUsername.isEmpty() || username.equals(friendUsername)) {
      throw new IllegalArgumentException("Illegal username input");
    }
    if(!friends.contains(friendUsername)) {
      friends.add(friendUsername);
    } else {
      throw new IllegalArgumentException("Friend already exists");
    }
  }

  /** Removes a friend's username from this user's friend list. */
  public void removeFriend(String friendUsername) {
    if(!friends.remove(friendUsername)){
      throw new IllegalArgumentException("Not a friend");
    }
  }
}
