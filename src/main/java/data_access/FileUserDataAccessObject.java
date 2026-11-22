package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeSet;
import models.User;
import use_case.change_password.ChangePasswordUserDataAccessInterface;
import use_case.login.LoginUserDataAccessInterface;
import use_case.profile.ProfileUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;

/**
 * A local-file DAO that persists accounts across application restarts.
 *
 * <p>This implementation is not a shared online account service. Friends and visibility
 * fields are retained for compatibility with the team's existing user model; pins must
 * be saved by the separate pin DAO owned by the map/profile developers.
 */
public final class FileUserDataAccessObject implements SignupUserDataAccessInterface,
    LoginUserDataAccessInterface, ChangePasswordUserDataAccessInterface,
    ProfileUserDataAccessInterface {
  private final Path file;
  private Map<String, User> users;

  /**
   * Creates the DAO and loads any previously registered accounts.
   *
   * @param file location of the local account store
   */
  public FileUserDataAccessObject(Path file) {
    this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
    this.users = load();
  }

  /** Returns whether an account is registered. */
  @Override
  public synchronized boolean existsByName(String username) {
    return users.containsKey(username);
  }

  /** Saves a new account atomically without replacing an existing username. */
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

  /** Returns a copy of the account, or null if the username does not exist. */
  @Override
  public synchronized User get(String username) {
    User user = users.get(username);
    return user == null ? null : copy(user);
  }

  /**
   * Replaces only an existing user's password hash, preserving other account fields.
   *
   * @param username existing username
   * @param hash encoded, securely derived new password hash
   */
  @Override
  public synchronized void updatePasswordHash(String username, String hash) {
    Objects.requireNonNull(hash, "hash");
    User original = users.get(username);
    if (original == null) {
      throw new IllegalStateException("Account no longer exists.");
    }
    Map<String, User> proposed = new HashMap<>(users);
    proposed.put(username, new User(username, hash, new ArrayList<>(original.getPins()),
        new ArrayList<>(original.getFriends()), original.isVisible()));
    persist(proposed);
    users = proposed;
  }

  /** Loads account records without creating a file when the store is absent. */
  private Map<String, User> load() {
    Map<String, User> result = new HashMap<>();
    if (!Files.exists(file)) {
      return result;
    }
    Properties properties = new Properties();
    try (InputStream input = Files.newInputStream(file)) {
      properties.load(input);
      String index = properties.getProperty("users", "");
      if (index.isEmpty()) {
        return result;
      }
      for (String username : index.split(",")) {
        String hash = properties.getProperty("hash." + username);
        if (hash == null || hash.isBlank()) {
          throw new IllegalStateException("Missing password hash for: " + username);
        }
        ArrayList<String> friends = new ArrayList<>();
        String savedFriends = properties.getProperty("friends." + username, "");
        if (!savedFriends.isEmpty()) {
          for (String friend : savedFriends.split(",")) {
            friends.add(friend);
          }
        }
        boolean visible = Boolean.parseBoolean(
            properties.getProperty("visible." + username, "false"));
        result.put(username, new User(username, hash, new ArrayList<>(), friends, visible));
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot read account file: " + file, exception);
    }
    return result;
  }

  /** Writes a replacement snapshot to a temporary file before committing it. */
  private void persist(Map<String, User> proposed) {
    Properties properties = new Properties();
    properties.setProperty("users", String.join(",", new TreeSet<>(proposed.keySet())));
    for (User user : proposed.values()) {
      String username = user.getUsername();
      properties.setProperty("hash." + username, user.getPasswordHash());
      properties.setProperty("friends." + username, String.join(",", user.getFriends()));
      properties.setProperty("visible." + username, Boolean.toString(user.isVisible()));
    }
    Path directory = file.getParent();
    Path temporary = null;
    try {
      Files.createDirectories(directory);
      temporary = Files.createTempFile(directory, "accounts-", ".tmp");
      try (OutputStream stream = Files.newOutputStream(temporary)) {
        properties.store(stream, "CSC207 account hashes (do not commit this file)");
      }
      try {
        Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot persist account: " + file, exception);
    } finally {
      if (temporary != null) {
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException ignored) {
          // Best-effort temporary-file cleanup; preserve the initial exception.
        }
      }
    }
  }

  /** Returns an independent entity so mutations cannot bypass persistence. */
  private static User copy(User user) {
    return new User(user.getUsername(), user.getPasswordHash(),
        new ArrayList<>(user.getPins()), new ArrayList<>(user.getFriends()),
        user.isVisible());
  }
}
