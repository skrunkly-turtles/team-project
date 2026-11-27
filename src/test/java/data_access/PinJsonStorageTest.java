package data_access;

import models.Book;
import models.Coordinate;
import models.Passage;
import models.Pin;
import models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks that pins round-trip through the JSON file and getAll returns copies. */
class PinJsonStorageTest {
    @TempDir
    Path directory;

    @Test
    void pinContentsSurviveRestart() {
        Path file = directory.resolve("users.json");
        JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(file);
        Pin pin = new Pin(3, "alice", new Coordinate(-79.4, 43.7),
                new Passage(new Book("Dune", "Frank Herbert", 1965), "text", 1), "desc", false);
        accounts.save(new User("alice", "hash", new ArrayList<>(List.of(pin)),
                new ArrayList<>(), new ArrayList<>(), false));

        Pin loaded = new JsonUserDataAccessObject(file).get("alice").getPins().get(0);
        assertEquals(3, loaded.getId());
        assertEquals("desc", loaded.getDescription());
        assertFalse(loaded.isVisible());
        assertEquals("Dune", loaded.getPassage().getBook().getTitle());
        assertEquals("text", loaded.getPassage().getText());
        assertEquals(43.7, loaded.getCoordinate().getLatitude());
    }

    @Test
    void getAllReturnsEveryAccountAsCopies() {
        JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(directory.resolve("users.json"));
        accounts.save(new User("alice", "hash", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false));
        accounts.save(new User("bob", "hash", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false));
        assertEquals(2, accounts.getAll().size());
        accounts.getAll().get(0).setVisible(true);
        assertFalse(accounts.get("alice").isVisible() && accounts.get("bob").isVisible());
    }

    @Test
    void editedPinSurvivesRestart() {
        Path file = directory.resolve("users.json");
        JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(file);
        accounts.save(new User("alice", "hash", new ArrayList<>(List.of(
                new Pin(1, "alice", new Coordinate(1, 2),
                        new Passage(new Book("B", "A", 2000), "text", 1), "d", true))),
                new ArrayList<>(), new ArrayList<>(), false));
        accounts.updatePin("alice", new Pin(1, "alice", new Coordinate(5, 6),
                new Passage(new Book("B2", "A2", 2001), "text2", 1), "d2", true));

        Pin loaded = new JsonUserDataAccessObject(file).get("alice").getPins().get(0);
        assertEquals(6.0, loaded.getCoordinate().getLatitude());
        assertEquals("text2", loaded.getPassage().getText());
    }

    @Test
    void updatePinRejectsMissingPin() {
        JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(directory.resolve("u.json"));
        accounts.save(new User("alice", "hash", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), false));
        assertThrows(IllegalStateException.class, () -> accounts.updatePin("alice",
                new Pin(9, "n", new Coordinate(0, 0),
                        new Passage(new Book("", "", 0), "t", 1), "", true)));
    }
}