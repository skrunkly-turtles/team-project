package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/** Verifies model compatibility, encapsulation, and defensive collection copies. */
class UserTest {
  /** Both historic constructor signatures remain supported. */
  @Test
  void preservesConstructorsAndUsername() {
    User user = new User("alice", "encoded-hash", new ArrayList<>(), new ArrayList<>());
    assertEquals("alice", user.getUsername());
    assertEquals("encoded-hash", user.getPasswordHash());
    assertFalse(user.isVisible());
    User visible = new User("bobby", "hash", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true);
    assertTrue(visible.isVisible());
    visible.setVisible(false);
    assertFalse(visible.isVisible());
  }

  /** The entity rejects null required fields. */
  @Test
  void rejectsNullInput() {
    assertThrows(NullPointerException.class,
        () -> new User(null, "hash", new ArrayList<>(), new ArrayList<>()));
    assertThrows(NullPointerException.class,
        () -> new User("alice", null, new ArrayList<>(), new ArrayList<>()));
  }

  /** Callers cannot mutate the internal account collections through getters. */
  @Test
  void protectsFriendList() {
    ArrayList<String> source = new ArrayList<>();
    source.add("bobby");
    User user = new User("alice", "hash", new ArrayList<>(), source);
    source.add("cathy");
    user.getFriends().add("david");
    assertEquals(1, user.getFriends().size());
    assertTrue(user.getPins().isEmpty());
  }
}
