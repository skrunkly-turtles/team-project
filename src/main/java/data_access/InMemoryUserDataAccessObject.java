package data_access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Pin;
import models.User;
import use_case.add_friend.AddFriendUserDataAccessInterface;
import use_case.change_password.ChangePasswordUserDataAccessInterface;
import use_case.edit_pin.EditPinUserDataAccessInterface;
import use_case.login.LoginUserDataAccessInterface;
import use_case.profile.ProfileUserDataAccessInterface;
import use_case.read_pins.ReadPinsUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;

/** An in-memory DAO for independently testing the account use cases. */
public final class InMemoryUserDataAccessObject implements SignupUserDataAccessInterface,
    LoginUserDataAccessInterface, ChangePasswordUserDataAccessInterface,
    ProfileUserDataAccessInterface, AddFriendUserDataAccessInterface, EditPinUserDataAccessInterface, ReadPinsUserDataAccessInterface {
  private final Map<String, User> users = new HashMap<>();

  /** Returns whether an account exists. */
  @Override
  public synchronized boolean existsByName(String username) {
    return users.containsKey(username);
  }

  /** Saves a new user, refusing to overwrite an existing account. */
  @Override
  public synchronized void save(User user) {
    if (users.containsKey(user.getUsername())) {
      throw new IllegalStateException("Username already exists.");
    }
    users.put(user.getUsername(), copy(user));
  }

  /** Returns a defensive copy of the given account or null. */
  @Override
  public synchronized User get(String username) {
    User user = users.get(username);
    return user == null ? null : copy(user);
  }

  /** Changes only the password hash, preserving account-related collections. */
  @Override
  public synchronized void updatePasswordHash(String username, String hash) {
    User old = users.get(username);
    if (old == null) {
      throw new IllegalStateException("Account no longer exists.");
    }
    users.put(username, new User(username, hash, new ArrayList<>(old.getPins()),
        new ArrayList<>(old.getFriends()), new ArrayList<>(old.getFriendRequests()), old.isVisible()));
  }

  @Override
  public synchronized void sendFriendRequest(String username, String friendUsername) {
    User sender = users.get(username);
    User recipient = users.get(friendUsername);
    if (sender == null || recipient == null) {
      throw new IllegalStateException("Account no longer exists.");
    }
    if (sender.getFriends().contains(friendUsername)
            || recipient.getFriendRequests().contains(username)
            || sender.getFriendRequests().contains(friendUsername)) {
      throw new IllegalStateException("Request not allowed.");
    }
    recipient.receiveFriendRequest(username);
  }
  /** Returns independent copies of every stored account. */
  @Override
  public synchronized List<User> getAll() {
    List<User> all = new ArrayList<>();
    for (User user : users.values()) {
      all.add(copy(user));
    }
    return all;
  }

  @Override
  public synchronized void updatePin(String username, Pin pin) {
    User old = users.get(username);
    if (old == null) {
      throw new IllegalStateException("Account no longer exists.");
    }
    List<Pin> pins = new ArrayList<>(old.getPins());
    boolean found = false;
    for (int i = 0; i < pins.size(); i++) {
      if (pins.get(i).getId() == pin.getId()) {
        pins.set(i, pin);
        found = true;
        break;
      }
    }
    if (!found) {
      throw new IllegalStateException("Pin no longer exists.");
    }
    users.put(username, new User(username, old.getPasswordHash(), new ArrayList<>(pins),
            new ArrayList<>(old.getFriends()), new ArrayList<>(old.getFriendRequests()),
            old.isVisible()));
  }


  /** Returns an independent entity to prevent mutations bypassing storage. */
  private static User copy(User user) {
    return new User(user.getUsername(), user.getPasswordHash(),
        new ArrayList<>(user.getPins()), new ArrayList<>(user.getFriends()), new ArrayList<>(user.getFriendRequests()),
        user.isVisible());
  }
}
