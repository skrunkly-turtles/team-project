package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;

/** Exercises persistent account creation and password changes after reopening the DAO. */
class FileUserDataAccessObjectTest {
  /** Registration and hashing survive multiple instances of the DAO. */
  @Test
  void savesAndReloadsAccount() throws IOException {
    Path file = Files.createTempDirectory("accounts-test-").resolve("accounts.properties");
    Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    String hash = hasher.hash("correctpassword");
    new FileUserDataAccessObject(file).save(new CommonUserFactory().create("alice", hash));
    FileUserDataAccessObject reopened = new FileUserDataAccessObject(file);
    assertTrue(reopened.existsByName("alice"));
    assertNotNull(reopened.get("alice"));
    assertTrue(hasher.matches("correctpassword", reopened.get("alice").getPasswordHash()));
    assertFalse(Files.readString(file).contains("correctpassword"));
  }

  /** Username collisions never overwrite an existing account. */
  @Test
  void rejectsDuplicateAccount() throws IOException {
    Path file = Files.createTempDirectory("accounts-test-").resolve("accounts.properties");
    FileUserDataAccessObject db = new FileUserDataAccessObject(file);
    db.save(new CommonUserFactory().create("alice", "old-hash"));
    assertThrows(IllegalStateException.class,
        () -> db.save(new CommonUserFactory().create("alice", "new-hash")));
    assertEquals("old-hash", new FileUserDataAccessObject(file)
        .get("alice").getPasswordHash());
  }

  /** A stored password hash can be changed without losing the account. */
  @Test
  void updatePasswordPersistsAfterRestart() throws IOException {
    Path file = Files.createTempDirectory("accounts-test-").resolve("accounts.properties");
    FileUserDataAccessObject db = new FileUserDataAccessObject(file);
    db.save(new CommonUserFactory().create("alice", "old-hash"));
    db.updatePasswordHash("alice", "new-hash");
    assertEquals("new-hash", new FileUserDataAccessObject(file)
        .get("alice").getPasswordHash());
    assertThrows(IllegalStateException.class,
        () -> db.updatePasswordHash("missing", "new-hash"));
  }

  /** A missing local database means an empty account collection, not an error. */
  @Test
  void emptyFileIsNotCreatedUntilSave() throws IOException {
    Path file = Files.createTempDirectory("accounts-test-").resolve("accounts.properties");
    FileUserDataAccessObject db = new FileUserDataAccessObject(file);
    assertFalse(db.existsByName("alice"));
    assertFalse(Files.exists(file));
  }

  /** Broken persisted records fail rather than silently manufacturing an account. */
  @Test
  void detectsBrokenRecord() throws IOException {
    Path file = Files.createTempDirectory("accounts-test-").resolve("accounts.properties");
    Files.writeString(file, "users=alice\n");
    assertThrows(IllegalStateException.class, () -> new FileUserDataAccessObject(file));
  }
}
