package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import models.Pin;
import models.User;
import use_case.change_password.ChangePasswordUserDataAccessInterface;
import use_case.login.LoginUserDataAccessInterface;
import use_case.profile.ProfileUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;

/** Stores local user accounts, friend lists, and pins in a JSON file. */
public final class JsonUserDataAccessObject implements SignupUserDataAccessInterface,
    LoginUserDataAccessInterface, ChangePasswordUserDataAccessInterface,
    ProfileUserDataAccessInterface {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Type PIN_LIST_TYPE = new TypeToken<ArrayList<Pin>>() { }.getType();

  private final Path file;
  private Map<String, User> users;

  /** Opens an existing JSON file or starts with an empty collection. */
  public JsonUserDataAccessObject(Path file) {
    this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
    users = load();
  }

  /** Returns whether the account already exists. */
  @Override
  public synchronized boolean existsByName(String username) {
    return users.containsKey(username);
  }

  /** Saves a new account, rejecting duplicate usernames. */
  @Override
  public synchronized void save(User user) {
    Objects.requireNonNull(user, "user");
    if (users.containsKey(user.getUsername())) {
      throw new IllegalStateException("Username already exists.");
    }
    Map<String, User> proposed = new HashMap<>(users);
    proposed.put(user.getUsername(), copy(user));
    persist(proposed);
    users = proposed;
  }

  /** Returns an independent copy of an account, or null when not found. */
  @Override
  public synchronized User get(String username) {
    User user = users.get(username);
    return user == null ? null : copy(user);
  }

  /** Updates the password without losing stored friends or pins. */
  @Override
  public synchronized void updatePasswordHash(String username, String encodedHash) {
    Objects.requireNonNull(encodedHash, "encodedHash");
    User current = requireUser(username);
    saveReplacement(new User(username, encodedHash, copyPins(current.getPins()),
        new ArrayList<>(current.getFriends()), current.isVisible()));
  }

  /** Persists a complete updated friend list for an existing account. */
  public synchronized void updateFriends(String username, List<String> friends) {
    Objects.requireNonNull(friends, "friends");
    if (friends.stream().anyMatch(friend -> friend == null || friend.isBlank())) {
      throw new IllegalArgumentException("Friend usernames must not be blank.");
    }
    User current = requireUser(username);
    saveReplacement(new User(username, current.getPasswordHash(), copyPins(current.getPins()),
        new ArrayList<>(friends), current.isVisible()));
  }

  /** Persists a complete updated pin list for an existing account. */
  public synchronized void updatePins(String username, List<Pin> pins) {
    Objects.requireNonNull(pins, "pins");
    if (pins.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("Pins must not contain null.");
    }
    User current = requireUser(username);
    saveReplacement(new User(username, current.getPasswordHash(), copyPins(pins),
        new ArrayList<>(current.getFriends()), current.isVisible()));
  }

  /** Persists a change to the user's visibility preference. */
  public synchronized void updateVisibility(String username, boolean visible) {
    User current = requireUser(username);
    saveReplacement(new User(username, current.getPasswordHash(), copyPins(current.getPins()),
        new ArrayList<>(current.getFriends()), visible));
  }

  /** Returns the existing account; updates must never manufacture users. */
  private User requireUser(String username) {
    User user = users.get(username);
    if (user == null) {
      throw new IllegalStateException("Account no longer exists: " + username);
    }
    return user;
  }

  /** Commits an updated snapshot only after the JSON file has been written. */
  private void saveReplacement(User updated) {
    Map<String, User> proposed = new HashMap<>(users);
    proposed.put(updated.getUsername(), copy(updated));
    persist(proposed);
    users = proposed;
  }

  /** Rebuilds the in-memory map from validated JSON records. */
  private Map<String, User> load() {
    Map<String, User> result = new HashMap<>();
    if (!Files.exists(file)) {
      return result;
    }
    try {
      String json = Files.readString(file, StandardCharsets.UTF_8);
      StoredFile data = GSON.fromJson(json, StoredFile.class);
      if (data == null || data.users == null) {
        throw new IllegalStateException("Invalid JSON account file: missing users list.");
      }
      for (StoredUser record : data.users) {
        if (record == null || record.username == null || record.username.isBlank()
            || record.passwordHash == null || record.passwordHash.isBlank()
            || record.friends == null || record.pins == null
            || record.friends.stream().anyMatch(friend -> friend == null || friend.isBlank())
            || record.pins.stream().anyMatch(Objects::isNull) || result.containsKey(record.username)) {
          throw new IllegalStateException("Invalid or duplicate account record in JSON file.");
        }
        result.put(record.username, new User(record.username, record.passwordHash,
            copyPins(record.pins), new ArrayList<>(record.friends), record.visible));
      }
      return result;
    } catch (IOException | JsonParseException exception) {
      throw new IllegalStateException("Could not read JSON account file: " + file, exception);
    }
  }

  /** Writes a complete JSON snapshot and replaces the old file atomically if supported. */
  private void persist(Map<String, User> snapshot) {
    StoredFile data = new StoredFile();
    for (String username : new TreeSet<>(snapshot.keySet())) {
      data.users.add(new StoredUser(snapshot.get(username)));
    }
    Path temporary = null;
    try {
      Path directory = file.getParent();
      Files.createDirectories(directory);
      temporary = Files.createTempFile(directory, "users-", ".tmp");
      Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
      try {
        Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Could not save JSON account file: " + file, exception);
    } finally {
      if (temporary != null) {
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException ignored) {
          // Best effort: leave the original failure visible.
        }
      }
    }
  }

  /** Copies an account without sharing its mutable pin objects with callers. */
  private static User copy(User user) {
    return new User(user.getUsername(), user.getPasswordHash(), copyPins(user.getPins()),
        new ArrayList<>(user.getFriends()), user.isVisible());
  }

  /** Copies pins using Gson because the team's Pin model does not expose all getters. */
  private static ArrayList<Pin> copyPins(List<Pin> pins) {
    return GSON.fromJson(GSON.toJson(pins), PIN_LIST_TYPE);
  }

  /** JSON root object containing all locally registered accounts. */
  private static final class StoredFile {
    private List<StoredUser> users = new ArrayList<>();
  }

  /** Data-transfer object that keeps the JSON schema separate from the entity constructor. */
  private static final class StoredUser {
    private String username;
    private String passwordHash;
    private List<String> friends;
    private List<Pin> pins;
    private boolean visible;

    /** Used by Gson when reading a JSON record. */
    private StoredUser() {
    }

    /** Creates a JSON record from an existing user. */
    private StoredUser(User user) {
      username = user.getUsername();
      passwordHash = user.getPasswordHash();
      friends = new ArrayList<>(user.getFriends());
      pins = copyPins(user.getPins());
      visible = user.isVisible();
    }
  }
}
