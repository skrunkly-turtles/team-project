package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import models.Book;
import models.CommonUserFactory;
import models.Coordinate;
import models.Passage;
import models.Pin;
import models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies JSON account storage, including nested friends and pin data. */
class JsonUserDataAccessObjectTest {
  @TempDir
  Path directory;

  /** Saved accounts persist and do not reveal raw passwords. */
  @Test
  void savesAndReloadsHashedAccount() throws IOException {
    Path file = directory.resolve("users.json");
    Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    JsonUserDataAccessObject dao = new JsonUserDataAccessObject(file);
    dao.save(new CommonUserFactory().create("alice", hasher.hash("password123")));

    assertTrue(dao.existsByName("alice"));
    assertFalse(Files.readString(file).contains("password123"));
    User reloaded = new JsonUserDataAccessObject(file).get("alice");
    assertNotNull(reloaded);
    assertTrue(hasher.matches("password123", reloaded.getPasswordHash()));
    assertTrue(Files.readString(file).contains("\"users\""));
  }

  /** Nested friends and pins are preserved after reopening the DAO. */
  @Test
  void savesAndReloadsFriendsAndPins() throws IOException {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject dao = new JsonUserDataAccessObject(file);
    dao.save(new CommonUserFactory().create("alice", "stored-hash"));
    dao.updateFriends("alice", List.of("bob", "charlie"));
    Pin pin = new Pin(7, "alice", new Coordinate(-79.38, 43.65),
        new Passage(new Book("Example Book", "Example Author", 2020),
            "A memorable line", 3), "Favourite passage", true);
    dao.updatePins("alice", List.of(pin));
    dao.updateVisibility("alice", true);

    User saved = new JsonUserDataAccessObject(file).get("alice");
    assertEquals(List.of("bob", "charlie"), saved.getFriends());
    assertEquals(1, saved.getPins().size());
    assertEquals("Favourite passage", saved.getPins().get(0).getDescription());
    assertTrue(saved.isVisible());
    String json = Files.readString(file);
    assertTrue(json.contains("\"pins\""));
    assertTrue(json.contains("\"coordinate\""));
    assertTrue(json.contains("\"passage\""));
  }

  /** Changing the password preserves associated friends, pins and visibility. */
  @Test
  void passwordUpdateKeepsOtherFields() {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject dao = new JsonUserDataAccessObject(file);
    dao.save(new CommonUserFactory().create("alice", "old-hash"));
    dao.updateFriends("alice", List.of("bob"));
    dao.updatePins("alice", List.of(new Pin(1, "alice",
        new Coordinate(-79.38, 43.65),
        new Passage(new Book("Book", "Author", 2020), "Passage", 1), "Pin", true)));
    dao.updateVisibility("alice", true);
    dao.updatePasswordHash("alice", "new-hash");

    User reloaded = new JsonUserDataAccessObject(file).get("alice");
    assertEquals("new-hash", reloaded.getPasswordHash());
    assertEquals(List.of("bob"), reloaded.getFriends());
    assertEquals(1, reloaded.getPins().size());
    assertTrue(reloaded.isVisible());
  }

  /** Invalid or duplicate records are rejected instead of silently ignored. */
  @Test
  void rejectsInvalidRecordsAndDuplicateAccounts() throws IOException {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject dao = new JsonUserDataAccessObject(file);
    dao.save(new CommonUserFactory().create("alice", "hash"));
    assertThrows(IllegalStateException.class,
        () -> dao.save(new CommonUserFactory().create("alice", "other-hash")));
    assertNull(dao.get("missing"));
    Files.writeString(file, "{\"users\":[{\"username\":\"alice\"}]}");
    assertThrows(IllegalStateException.class, () -> new JsonUserDataAccessObject(file));
    Files.writeString(file, "not valid JSON");
    assertThrows(IllegalStateException.class, () -> new JsonUserDataAccessObject(file));
  }

  /** Missing storage does not create a file until an account is saved. */
  @Test
  void missingFileStartsEmpty() {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject dao = new JsonUserDataAccessObject(file);
    assertFalse(dao.existsByName("alice"));
    assertFalse(Files.exists(file));
  }

  @Test
  void deletedPinStaysDeletedAfterRestart() {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(file);
    accounts.save(new User("alice", "hash", new ArrayList<>(List.of(
            new Pin(1, "alice", new Coordinate(1, 2),
                    new Passage(new Book("B", "A", 2000), "t", 1), "d", true),
            new Pin(2, "alice", new Coordinate(3, 4),
                    new Passage(new Book("B", "A", 2000), "t", 1), "d", true))),
            new ArrayList<>(), new ArrayList<>(), false));
    accounts.deletePin("alice", 1);

    List<Pin> loaded = new JsonUserDataAccessObject(file).get("alice").getPins();
    assertEquals(1, loaded.size());
    assertEquals(2, loaded.get(0).getId());
  }

  @Test
  void deletePinRejectsMissingPin() {
    JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(directory.resolve("u.json"));
    accounts.save(new User("alice", "hash", new ArrayList<>(), new ArrayList<>(),
            new ArrayList<>(), false));
    assertThrows(IllegalStateException.class, () -> accounts.deletePin("alice", 9));
  }
}
