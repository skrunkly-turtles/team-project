package use_case.add_friend;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import models.CommonUserFactory;
import models.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks authentication, validation, and persistence for the Add Friend use case. */
class AddFriendInteractorTest {
    /** Test double for presenter notifications. */
    private static final class Output implements AddFriendOutputBoundary {
        private String error;
        private AddFriendOutputData success;

        /** Records the use case's success data. */
        @Override
        public void prepareSuccessView(AddFriendOutputData output, String message) {
            success = output;
        }

        /** Records the use case's failure message. */
        @Override
        public void prepareFailureView(String message) {
            error = message;
        }
    }

    /** Delegates to the in-memory DAO but fails every friendship write. */
    private static final class FailingWriteAccounts implements AddFriendUserDataAccessInterface {
        private final InMemoryUserDataAccessObject delegate;

        private FailingWriteAccounts(InMemoryUserDataAccessObject delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean existsByName(String username) {
            return delegate.existsByName(username);
        }

        @Override
        public User get(String username) {
            return delegate.get(username);
        }

        @Override
        public void sendFriendRequest(String username, String friendUsername) {
            throw new IllegalStateException("Simulated storage failure.");
        }
    }

    /** Contains a complete in-memory test fixture. */
    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final AddFriendInteractor interactor =
                new AddFriendInteractor(accounts, output, session);

        /** Creates alice and bob, with alice signed in. */
        private void signedInWithTwoUsers() {
            accounts.save(new CommonUserFactory().create("alice", "hash"));
            accounts.save(new CommonUserFactory().create("bob", "hash"));
            session.signIn("alice");
        }

        /** Submits the add-friend request. */
        private void add(String friendUsername) {
            interactor.execute(new AddFriendInputData(friendUsername));
        }
    }

    /** Sending a request records it on the recipient and creates no friendship. */
    @Test
    void sendsRequestToRecipient() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("bob");
        assertNull(fixture.output.error);
        assertEquals("alice", fixture.output.success.getUsername());
        assertEquals("bob", fixture.output.success.getFriendUsername());
        assertTrue(fixture.accounts.get("bob").getFriendRequests().contains("alice"));
        assertTrue(fixture.accounts.get("alice").getFriendRequests().isEmpty());
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
        assertTrue(fixture.accounts.get("bob").getFriends().isEmpty());
    }

    /** One user can have requests pending with several people at once. */
    @Test
    void allowsRequestsToMultipleUsers() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.accounts.save(new CommonUserFactory().create("carol", "hash"));
        fixture.add("bob");
        fixture.add("carol");
        assertTrue(fixture.accounts.get("bob").getFriendRequests().contains("alice"));
        assertTrue(fixture.accounts.get("carol").getFriendRequests().contains("alice"));
    }

    /** A username that is already a friend is not added a second time. */
    @Test
    void userRejectsDuplicateFriend() {
        User alice = new CommonUserFactory().create("alice", "hash");
        alice.addFriend("bob");
        assertThrows(IllegalArgumentException.class, () -> alice.addFriend("bob"));
        assertEquals(1, alice.getFriends().size());
    }

    /** If the other user already requested you, you are told to accept instead. */
    @Test
    void rejectsRequestWhenOtherUserAlreadyRequested() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("bob");
        fixture.session.signIn("bob");
        fixture.output.success = null;
        fixture.add("alice");
        assertTrue(fixture.output.error.contains("already sent you"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("alice").getFriendRequests().isEmpty());
    }

    /** Only usernames that exist in the repository can be added. */
    @Test
    void rejectsNonexistentUser() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("nobody");
        assertTrue(fixture.output.error.contains("Account does not exist"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
        assertTrue(fixture.accounts.get("bob").getFriendRequests().isEmpty());
        assertFalse(fixture.accounts.existsByName("nobody"));
    }

    /** A user cannot add themselves. */
    @Test
    void rejectsAddingSelf() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("alice");
        assertTrue(fixture.output.error.contains("yourself"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
    }

    /** Adding a friend always requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.accounts.save(new CommonUserFactory().create("bob", "hash"));
        fixture.add("bob");
        assertNotNull(fixture.output.error);
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("bob").getFriends().isEmpty());
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.accounts.save(new CommonUserFactory().create("bob", "hash"));
        fixture.session.signIn("alice");
        fixture.add("bob");
        assertTrue(fixture.output.error.equals("Account does not exist"));
        assertNull(fixture.output.success);
    }

    /** A null username is rejected. */
    @Test
    void rejectsNullUsername() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add(null);
        assertTrue(fixture.output.error.contains("Please enter a friend name"));
        assertNull(fixture.output.success);
    }

    /** An empty username is rejected. */
    @Test
    void rejectsEmptyUsername() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("");
        assertTrue(fixture.output.error.contains("Please enter a friend name"));
        assertNull(fixture.output.success);
    }

    /** A second request to the same user is rejected and not stored twice. */
    @Test
    void rejectsDuplicateRequest() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        fixture.add("bob");
        fixture.output.success = null;
        fixture.add("bob");
        assertTrue(fixture.output.error.contains("already sent"));
        assertNull(fixture.output.success);
        assertEquals(1, fixture.accounts.get("bob").getFriendRequests().size());
    }

    /** Existing friends cannot be sent a request. */
    @Test
    void rejectsRequestToExistingFriend() {
        Fixture fixture = new Fixture();
        fixture.accounts.save(new User("alice", "hash", new ArrayList<>(),
                new ArrayList<>(List.of("bob")), new ArrayList<>(), false));
        fixture.accounts.save(new User("bob", "hash", new ArrayList<>(),
                new ArrayList<>(List.of("alice")), new ArrayList<>(), false));
        fixture.session.signIn("alice");
        fixture.add("bob");
        assertTrue(fixture.output.error.contains("already friends"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("bob").getFriendRequests().isEmpty());
    }

    /** A storage failure is reported as a failure and not as a success. */
    @Test
    void reportsStorageFailure() {
        Fixture fixture = new Fixture();
        fixture.signedInWithTwoUsers();
        Output output = new Output();
        AddFriendInteractor interactor = new AddFriendInteractor(
                new FailingWriteAccounts(fixture.accounts), output, fixture.session);
        interactor.execute(new AddFriendInputData("bob"));
        assertTrue(output.error.contains("Could not send request"));
        assertNull(output.success);
    }
}