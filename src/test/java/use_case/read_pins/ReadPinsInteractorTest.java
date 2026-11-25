package use_case.read_pins;

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

/** Checks the access rules and contents for the Read Pins use case. */
class ReadPinsInteractorTest {
    /** Test double for presenter notifications. */
    private static final class Output implements ReadPinsOutputBoundary {
        private String error;
        private ReadPinsOutputData success;

        @Override
        public void prepareSuccessView(ReadPinsOutputData output) {
            success = output;
        }

        @Override
        public void prepareFailureView(String message) {
            error = message;
        }

        private List<String> labels() {
            return success.getPins().stream().map(p -> p.owner() + "#" + p.id()).toList();
        }
    }

    /** Contains a complete in-memory test fixture. */
    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final ReadPinsInteractor interactor =
                new ReadPinsInteractor(accounts, output, session);

        private void addUser(String name, List<String> friends, List<Pin> pins) {
            accounts.save(new User(name, "hash", new ArrayList<>(pins),
                    new ArrayList<>(friends), new ArrayList<>(), false));
        }

        private void readAs(String viewer) {
            session.signIn(viewer);
            interactor.execute();
        }
    }

    private static Pin pin(int id, String owner, boolean visible) {
        return new Pin(id, owner, new Coordinate(-79.4, 43.7),
                new Passage(new Book("Dune", "Frank Herbert", 1965), "passage " + id, 2),
                "desc " + id, visible);
    }

    /** Users always see all of their own pins, public or private. */
    @Test
    void seesOwnPins() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of(pin(1, "alice", true), pin(2, "alice", false)));
        fixture.readAs("alice");
        assertEquals(List.of("alice#1", "alice#2"), fixture.output.labels());
    }

    /** Strangers' public pins are visible and their private pins are not. */
    @Test
    void seesOnlyPublicPinsOfStrangers() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of());
        fixture.addUser("bob", List.of(), List.of(pin(1, "bob", true), pin(2, "bob", false)));
        fixture.readAs("alice");
        assertEquals(List.of("bob#1"), fixture.output.labels());
    }

    /** Friends' private pins are visible too. */
    @Test
    void seesPrivatePinsOfFriends() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of());
        fixture.addUser("bob", List.of("alice"), List.of(pin(1, "bob", true), pin(2, "bob", false)));
        fixture.readAs("alice");
        assertEquals(List.of("bob#1", "bob#2"), fixture.output.labels());
    }

    /** The owner's friend list decides who sees private pins. */
    @Test
    void friendshipIsDecidedByTheOwnersList() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of());
        fixture.addUser("bob", List.of(), List.of(pin(2, "bob", false)));
        fixture.readAs("alice");
        assertTrue(fixture.output.labels().isEmpty());
    }

    /** Pins from several users are combined and ordered by owner, then id. */
    @Test
    void combinesPinsAcrossUsersInOrder() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of(pin(2, "alice", false), pin(1, "alice", true)));
        fixture.addUser("bob", List.of(), List.of(pin(1, "bob", true)));
        fixture.addUser("carol", List.of("alice"), List.of(pin(1, "carol", false)));
        fixture.readAs("alice");
        assertEquals(List.of("alice#1", "alice#2", "bob#1", "carol#1"),
                fixture.output.labels());
    }

    /** The output says who created the pin and carries all of its contents. */
    @Test
    void outputCarriesCreatorAndContents() {
        Fixture fixture = new Fixture();
        fixture.addUser("bob", List.of(), List.of(pin(7, "bob", true)));
        fixture.addUser("alice", List.of(), List.of());
        fixture.readAs("alice");
        Pin.PinData data = fixture.output.success.getPins().get(0);
        assertEquals(7, data.id());
        assertEquals("bob", data.owner());
        assertEquals("desc 7", data.description());
        assertEquals("Dune", data.passage().getBook().getTitle());
        assertEquals("Frank Herbert", data.passage().getBook().getAuthor());
        assertEquals(1965, data.passage().getBook().getYear());
        assertEquals("passage 7", data.passage().getText());
        assertEquals(2, data.passage().getWordCount());
        assertEquals(43.7, data.coordinate().getLatitude());
        assertEquals(-79.4, data.coordinate().getLongitude());
        assertTrue(data.visible());
    }

    /** A pin with missing parts is shown with blanks instead of crashing. */
    @Test
    void toleratesPinsWithMissingParts() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(),
                List.of(new Pin(1, "alice", null, null, null, true)));
        fixture.readAs("alice");
        Pin.PinData data = fixture.output.success.getPins().get(0);
        assertNull(data.description());
        assertNull(data.passage());
    }

    /** No accessible pins is a success with an empty list. */
    @Test
    void noPinsGivesEmptySuccess() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of());
        fixture.readAs("alice");
        assertNull(fixture.output.error);
        assertTrue(fixture.output.success.getPins().isEmpty());
    }

    /** Reading pins requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("bob", List.of(), List.of(pin(1, "bob", true)));
        fixture.interactor.execute();
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.addUser("bob", List.of(), List.of(pin(1, "bob", true)));
        fixture.readAs("alice");
        assertEquals("Your account could not be found.", fixture.output.error);
        assertNull(fixture.output.success);
    }
}