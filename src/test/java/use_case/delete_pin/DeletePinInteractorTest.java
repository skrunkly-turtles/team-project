package use_case.delete_pin;

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

/** Checks ownership, visibility afterwards, and persistence for the Delete Pin use case. */
class DeletePinInteractorTest {
    private static final class Output implements DeletePinOutputBoundary {
        private String error;
        private DeletePinOutputData success;

        @Override
        public void prepareSuccessView(DeletePinOutputData output, String message) {
            success = output;
        }

        @Override
        public void prepareFailureView(String message) {
            error = message;
        }
    }

    private static final class FailingWriteAccounts implements DeletePinUserDataAccessInterface {
        private final InMemoryUserDataAccessObject delegate;

        private FailingWriteAccounts(InMemoryUserDataAccessObject delegate) {
            this.delegate = delegate;
        }

        @Override
        public User get(String username) {
            return delegate.get(username);
        }

        @Override
        public void deletePin(String username, int pinId) {
            throw new IllegalStateException("Simulated storage failure.");
        }
    }

    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final DeletePinInteractor interactor =
                new DeletePinInteractor(accounts, output, session);

        private void addUser(String name, List<Pin> pins) {
            accounts.save(new User(name, "hash", new ArrayList<>(pins), new ArrayList<>(),
                    new ArrayList<>(), false));
        }

        private void delete(int id) {
            interactor.execute(new DeletePinInputData(id));
        }

        private List<Integer> pinIds(String username) {
            return accounts.get(username).getPins().stream().map(Pin::getId).toList();
        }
    }

    private static Pin pin(int id, String owner) {
        return new Pin(id, owner, new Coordinate(-79.4, 43.7),
                new Passage(new Book("Dune", "Frank Herbert", 1965), "text", 1),
                "desc", true);
    }

    /** The owner can delete a pin and it is gone from the repository. */
    @Test
    void ownerDeletesPin() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.session.signIn("alice");
        fixture.delete(1);
        assertNull(fixture.output.error);
        assertEquals("alice", fixture.output.success.getUsername());
        assertEquals(1, fixture.output.success.getPinId());
        assertTrue(fixture.pinIds("alice").isEmpty());
    }

    /** Deleting one pin leaves the user's other pins alone. */
    @Test
    void keepsOtherPins() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice"), pin(2, "alice"), pin(3, "alice")));
        fixture.session.signIn("alice");
        fixture.delete(2);
        assertEquals(List.of(1, 3), fixture.pinIds("alice"));
    }

    /** Once deleted, the pin no longer exists in any account, so nobody can see it. */
    @Test
    void deletedPinIsGoneForEveryone() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.addUser("bob", List.of());
        fixture.session.signIn("alice");
        fixture.delete(1);
        assertTrue(fixture.accounts.getAll().stream()
                .allMatch(user -> user.getPins().isEmpty()));
    }

    /** A pin that belongs to someone else cannot be deleted. */
    @Test
    void cannotDeleteAnotherUsersPin() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of());
        fixture.addUser("bob", List.of(pin(1, "bob")));
        fixture.session.signIn("alice");
        fixture.delete(1);
        assertEquals("You can only delete pins you created.", fixture.output.error);
        assertNull(fixture.output.success);
        assertEquals(List.of(1), fixture.pinIds("bob"));
    }

    /** An id the user does not have is rejected. */
    @Test
    void rejectsUnknownPinId() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.session.signIn("alice");
        fixture.delete(99);
        assertTrue(fixture.output.error.contains("only delete pins you created"));
        assertEquals(List.of(1), fixture.pinIds("alice"));
    }

    /** Deleting requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.delete(1);
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
        assertEquals(List.of(1), fixture.pinIds("alice"));
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.session.signIn("alice");
        fixture.delete(1);
        assertEquals("Your account could not be found.", fixture.output.error);
        assertNull(fixture.output.success);
    }

    /** A storage failure is reported and nothing changes. */
    @Test
    void reportsStorageFailure() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(pin(1, "alice")));
        fixture.session.signIn("alice");
        Output output = new Output();
        new DeletePinInteractor(new FailingWriteAccounts(fixture.accounts), output,
                fixture.session).execute(new DeletePinInputData(1));
        assertTrue(output.error.contains("Could not delete pin"));
        assertNull(output.success);
        assertEquals(List.of(1), fixture.pinIds("alice"));
    }
}