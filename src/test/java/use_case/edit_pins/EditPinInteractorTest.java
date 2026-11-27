package use_case.edit_pin;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import models.Book;
import models.Coordinate;
import models.Passage;
import models.Pin;
import models.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks ownership, validation, and persistence for the Edit Pin use case. */
class EditPinInteractorTest {
    private static final int LIMIT = 5;

    private static final class Output implements EditPinOutputBoundary {
        private String error;
        private EditPinOutputData success;

        @Override
        public void prepareSuccessView(EditPinOutputData output, String message) {
            success = output;
        }

        @Override
        public void prepareFailureView(String message) {
            error = message;
        }
    }

    private static final class FailingWriteAccounts implements EditPinUserDataAccessInterface {
        private final InMemoryUserDataAccessObject delegate;

        private FailingWriteAccounts(InMemoryUserDataAccessObject delegate) {
            this.delegate = delegate;
        }

        @Override
        public User get(String username) {
            return delegate.get(username);
        }

        @Override
        public void updatePin(String username, Pin pin) {
            throw new IllegalStateException("Simulated storage failure.");
        }
    }

    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final EditPinInteractor interactor =
                new EditPinInteractor(accounts, output, session, LIMIT);

        private void addUser(String name, List<Pin> pins) {
            accounts.save(new User(name, "hash", new ArrayList<>(pins), new ArrayList<>(),
                    new ArrayList<>(), false));
        }

        private void aliceWithPin() {
            addUser("alice", List.of(pin(1, "alice")));
            session.signIn("alice");
        }

        private void edit(int id, String lat, String lon, String text) {
            interactor.execute(new EditPinInputData(id, "new desc", lat, lon, text,
                    "New Book", "New Author", "1999"));
        }
    }

    private static Pin pin(int id, String owner) {
        return new Pin(id, owner, new Coordinate(-79.4, 43.7),
                new Passage(new Book("Dune", "Frank Herbert", 1965), "old text", 2),
                "old desc", false);
    }

    /** All editable contents are replaced and written to the repository. */
    @Test
    void editsAllContentsAndPersists() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "10.5", "-20.25", "one two three");
        assertNull(fixture.output.error);
        assertEquals(1, fixture.output.success.getPinId());
        Pin saved = fixture.accounts.get("alice").getPins().get(0);
        assertEquals("new desc", saved.getDescription());
        assertEquals(10.5, saved.getCoordinate().getLatitude());
        assertEquals(-20.25, saved.getCoordinate().getLongitude());
        assertEquals("one two three", saved.getPassage().getText());
        assertEquals(3, saved.getPassage().getWordCount());
        assertEquals("New Book", saved.getPassage().getBook().getTitle());
        assertEquals("New Author", saved.getPassage().getBook().getAuthor());
        assertEquals(1999, saved.getPassage().getBook().getYear());
    }

    /** Surrounding whitespace is trimmed and the word count is recomputed. */
    @Test
    void trimsPassageAndRecomputesWordCount() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "1", "1", "  one two  ");
        Pin saved = fixture.accounts.get("alice").getPins().get(0);
        assertEquals("one two", saved.getPassage().getText());
        assertEquals(2, saved.getPassage().getWordCount());
    }

    /** Id, owner and visibility are not changed by an edit. */
    @Test
    void keepsIdOwnerAndVisibility() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "1", "1", "text");
        Pin saved = fixture.accounts.get("alice").getPins().get(0);
        assertEquals(1, saved.getId());
        assertEquals("alice", saved.getOwner());
        assertFalse(saved.isVisible());
    }

    /** Only the edited pin changes. */
    @Test
    void leavesOtherPinsUntouched() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice"), pin(2, "alice")));
        fixture.session.signIn("alice");
        fixture.edit(1, "1", "1", "text");
        assertEquals("old desc", fixture.accounts.get("alice").getPins().get(1).getDescription());
    }

    /** A pin that belongs to someone else cannot be edited. */
    @Test
    void cannotEditAnotherUsersPin() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of());
        fixture.addUser("bob", List.of(pin(1, "bob")));
        fixture.session.signIn("alice");
        fixture.edit(1, "1", "1", "text");
        assertEquals("You can only edit pins you created.", fixture.output.error);
        assertNull(fixture.output.success);
        assertEquals("old desc", fixture.accounts.get("bob").getPins().get(0).getDescription());
    }

    /** An id that does not exist among the user's pins is rejected. */
    @Test
    void rejectsUnknownPinId() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(99, "1", "1", "text");
        assertTrue(fixture.output.error.contains("only edit pins you created"));
        assertNull(fixture.output.success);
    }

    /** A blank passage is rejected and nothing is saved. */
    @Test
    void rejectsBlankPassage() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "1", "1", "   ");
        assertEquals("Passage is required.", fixture.output.error);
        assertNull(fixture.output.success);
        assertEquals("old desc", fixture.accounts.get("alice").getPins().get(0).getDescription());
    }

    /** The passage may have at most the configured number of words. */
    @Test
    void enforcesWordLimit() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "1", "1", "one two three four five six");
        assertTrue(fixture.output.error.contains("at most 5 words"));
        assertNull(fixture.output.success);
        fixture.output.error = null;
        fixture.edit(1, "1", "1", "one two three four five");
        assertNull(fixture.output.error);
        assertNotNull(fixture.output.success);
    }

    /** Non-numeric and out-of-range coordinates are rejected. */
    @Test
    void rejectsBadCoordinates() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "abc", "1", "text");
        assertEquals("Latitude and longitude must be numbers.", fixture.output.error);
        fixture.edit(1, "91", "1", "text");
        assertEquals("Latitude must be between -90 and 90.", fixture.output.error);
        fixture.edit(1, "1", "-181", "text");
        assertEquals("Longitude must be between -180 and 180.", fixture.output.error);
        assertNull(fixture.output.success);
        assertEquals(43.7, fixture.accounts.get("alice").getPins().get(0)
                .getCoordinate().getLatitude());
    }

    /** Boundary coordinates are accepted. */
    @Test
    void acceptsBoundaryCoordinates() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.edit(1, "-90", "180", "text");
        assertNull(fixture.output.error);
    }

    /** A year that is not a whole number is rejected. */
    @Test
    void rejectsBadYear() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        fixture.interactor.execute(new EditPinInputData(1, "d", "1", "1", "text",
                "b", "a", "nineteen"));
        assertEquals("Year must be a whole number.", fixture.output.error);
        assertNull(fixture.output.success);
    }

    /** Editing requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.edit(1, "1", "1", "text");
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.session.signIn("alice");
        fixture.edit(1, "1", "1", "text");
        assertEquals("Your account could not be found.", fixture.output.error);
        assertNull(fixture.output.success);
    }

    /** A storage failure is reported and nothing changes. */
    @Test
    void reportsStorageFailure() {
        Fixture fixture = new Fixture();
        fixture.aliceWithPin();
        Output output = new Output();
        new EditPinInteractor(new FailingWriteAccounts(fixture.accounts), output,
                fixture.session, LIMIT).execute(new EditPinInputData(1, "d", "1", "1",
                "text", "b", "a", "2000"));
        assertTrue(output.error.contains("Could not save changes"));
        assertNull(output.success);
        assertEquals("old desc", fixture.accounts.get("alice").getPins().get(0).getDescription());
    }
}