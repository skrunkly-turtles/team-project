package use_case.respond_friend;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import models.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks authentication, validation, and persistence for the Respond Friend use case. */
class RespondFriendInteractorTest {
    /** Test double for presenter notifications. */
    private static final class Output implements RespondFriendOutputBoundary {
        private String error;
        private String message;
        private RespondFriendOutputData success;

        @Override
        public void prepareSuccessView(RespondFriendOutputData output, String message) {
            success = output;
            this.message = message;
        }

        @Override
        public void prepareFailureView(String message) {
            error = message;
        }
    }

    /** Delegates reads to the in-memory DAO but fails every write. */
    private static final class FailingWriteAccounts implements RespondFriendUserDataAccessInterface {
        private final InMemoryUserDataAccessObject delegate;

        private FailingWriteAccounts(InMemoryUserDataAccessObject delegate) {
            this.delegate = delegate;
        }

        @Override
        public User get(String username) {
            return delegate.get(username);
        }

        @Override
        public void acceptFriendRequest(String username, String requesterUsername) {
            throw new IllegalStateException("Simulated storage failure.");
        }

        @Override
        public void declineFriendRequest(String username, String requesterUsername) {
            throw new IllegalStateException("Simulated storage failure.");
        }
    }

    /** Contains a complete in-memory test fixture. */
    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final RespondFriendInteractor interactor =
                new RespondFriendInteractor(accounts, output, session);

        /** Saves a user with the given friends and pending requests. */
        private void addUser(String name, List<String> friends, List<String> requests) {
            accounts.save(new User(name, "hash", new ArrayList<>(),
                    new ArrayList<>(friends), new ArrayList<>(requests), false));
        }

        /** Creates alice with a pending request from bob, and signs alice in. */
        private void aliceHasRequestFromBob() {
            addUser("alice", List.of(), List.of("bob"));
            addUser("bob", List.of(), List.of());
            session.signIn("alice");
        }

        private void respond(String requester, boolean accept) {
            interactor.execute(new RespondFriendInputData(requester, accept));
        }
    }

    /** Accepting makes both users friends and clears the request. */
    @Test
    void acceptAddsFriendToBothProfiles() {
        Fixture fixture = new Fixture();
        fixture.aliceHasRequestFromBob();
        fixture.respond("bob", true);
        assertNull(fixture.output.error);
        assertTrue(fixture.output.success.isAccepted());
        assertTrue(fixture.output.message.contains("now friends with bob"));
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriends());
        assertEquals(List.of("alice"), fixture.accounts.get("bob").getFriends());
        assertTrue(fixture.accounts.get("alice").getFriendRequests().isEmpty());
    }

    /** The output carries the updated lists for the presenter. */
    @Test
    void acceptOutputContainsUpdatedLists() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of("bob", "carol"));
        fixture.addUser("bob", List.of(), List.of());
        fixture.addUser("carol", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertEquals(List.of("bob"), fixture.output.success.getFriends());
        assertEquals(List.of("carol"), fixture.output.success.getFriendRequests());
    }

    /** Accepting also clears a request that went the other way. */
    @Test
    void acceptClearsReverseRequest() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of("bob"));
        fixture.addUser("bob", List.of(), List.of("alice"));
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertTrue(fixture.accounts.get("bob").getFriendRequests().isEmpty());
    }

    /** Accepting does not disturb existing friends. */
    @Test
    void acceptKeepsExistingFriends() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("carol"), List.of("bob"));
        fixture.addUser("bob", List.of(), List.of());
        fixture.addUser("carol", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertTrue(fixture.accounts.get("alice").getFriends().contains("carol"));
        assertTrue(fixture.accounts.get("alice").getFriends().contains("bob"));
        assertEquals(List.of("alice"), fixture.accounts.get("carol").getFriends());
    }

    /** Declining removes the request and creates no friendship. */
    @Test
    void declineRemovesRequestOnly() {
        Fixture fixture = new Fixture();
        fixture.aliceHasRequestFromBob();
        fixture.respond("bob", false);
        assertNull(fixture.output.error);
        assertFalse(fixture.output.success.isAccepted());
        assertTrue(fixture.output.message.contains("Declined"));
        assertTrue(fixture.accounts.get("alice").getFriendRequests().isEmpty());
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
        assertTrue(fixture.accounts.get("bob").getFriends().isEmpty());
    }

    /** Declining one request leaves the others pending. */
    @Test
    void declineKeepsOtherRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of("bob", "carol"));
        fixture.addUser("bob", List.of(), List.of());
        fixture.addUser("carol", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", false);
        assertEquals(List.of("carol"), fixture.accounts.get("alice").getFriendRequests());
        assertEquals(List.of("carol"), fixture.output.success.getFriendRequests());
    }

    /** A request from someone who is already a friend can be declined to clean it up. */
    @Test
    void declineAllowedForStaleRequestFromFriend() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of("bob"));
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", false);
        assertNull(fixture.output.error);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriends());
        assertTrue(fixture.accounts.get("alice").getFriendRequests().isEmpty());
    }

    /** Accepting a stale request from an existing friend is rejected. */
    @Test
    void acceptRejectedForExistingFriend() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of("bob"));
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertTrue(fixture.output.error.contains("already friends"));
        assertNull(fixture.output.success);
    }

    /** Responding requires a pending request, for both accept and decline. */
    @Test
    void rejectsWhenNoPendingRequest() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of());
        fixture.addUser("bob", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertTrue(fixture.output.error.contains("No pending request from bob"));
        assertNull(fixture.output.success);
        fixture.output.error = null;
        fixture.respond("bob", false);
        assertTrue(fixture.output.error.contains("No pending request from bob"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
    }

    /** Responding always requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of("bob"));
        fixture.addUser("bob", List.of(), List.of());
        fixture.respond("bob", true);
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriendRequests());
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.addUser("bob", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.respond("bob", true);
        assertEquals("Your account could not be found.", fixture.output.error);
        assertNull(fixture.output.success);
    }

    /** A null requester is rejected. */
    @Test
    void rejectsNullRequester() {
        Fixture fixture = new Fixture();
        fixture.aliceHasRequestFromBob();
        fixture.respond(null, true);
        assertTrue(fixture.output.error.contains("Please select a request"));
        assertNull(fixture.output.success);
    }

    /** An empty requester is rejected. */
    @Test
    void rejectsEmptyRequester() {
        Fixture fixture = new Fixture();
        fixture.aliceHasRequestFromBob();
        fixture.respond("", false);
        assertTrue(fixture.output.error.contains("Please select a request"));
        assertNull(fixture.output.success);
    }

    /** A requester whose account no longer exists cannot be accepted. */
    @Test
    void acceptFailsWhenRequesterAccountIsGone() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of(), List.of("ghost"));
        fixture.session.signIn("alice");
        fixture.respond("ghost", true);
        assertTrue(fixture.output.error.contains("Could not respond"));
        assertNull(fixture.output.success);
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
    }

    /** A storage failure is reported as a failure for both responses. */
    @Test
    void reportsStorageFailure() {
        Fixture fixture = new Fixture();
        fixture.aliceHasRequestFromBob();
        Output output = new Output();
        RespondFriendInteractor interactor = new RespondFriendInteractor(
                new FailingWriteAccounts(fixture.accounts), output, fixture.session);
        interactor.execute(new RespondFriendInputData("bob", true));
        assertTrue(output.error.contains("Could not respond"));
        assertNull(output.success);
        output.error = null;
        interactor.execute(new RespondFriendInputData("bob", false));
        assertTrue(output.error.contains("Could not respond"));
        assertNull(output.success);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriendRequests());
    }
}