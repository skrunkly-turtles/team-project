package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A registered application user, preserving the team's original account/pin/friend model.
 *
 * <p>The {@code passwordHash} field contains a one-way password hash, never plaintext.
 * Pin and friendship use cases belong to the other team members.
 */
public final class User {
  private final String username;
  private final String passwordHash;
  private final ArrayList<Pin> pins;
  private final ArrayList<String> friends;
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
      ArrayList<String> friends, boolean visible) {
    this.username = Objects.requireNonNull(username, "username");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.pins = new ArrayList<>(Objects.requireNonNull(pins, "pins"));
    this.friends = new ArrayList<>(Objects.requireNonNull(friends, "friends"));
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
    this(username, passwordHash, pins, friends, false);
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
}
