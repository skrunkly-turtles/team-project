package use_case.remove_friend;

import use_case.remove_friend.RemoveFriendDataAccessInterface;
import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import models.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks authentication, validation, and persistence for the Remove Friend use case. */
class RemoveFriendInteractorTest {
    /** Test double for presenter notifications. */
    private static final class Output implements RemoveFriendOutputBoundary {
        private String error;
        private RemoveFriendOutputData success;

        /** Records the use case's success data. */
        @Override
        public void prepareSuccessView(RemoveFriendOutputData output, String message) {
            success = output;
        }

        /** Records the use case's failure message. */
        @Override
        public void prepareFailureView(String message) {
            error = message;
        }
    }

    /** Delegates to the in-memory DAO but fails every removal write. */
    private static final class FailingWriteAccounts implements RemoveFriendDataAccessInterface {
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
        public void removeFriend(String username, String friendUsername) {
            throw new IllegalStateException("Simulated storage failure.");
        }
    }

    /** Contains a complete in-memory test fixture. */
    private static final class Fixture {
        private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
        private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
        private final Output output = new Output();
        private final RemoveFriendInteractor interactor =
                new RemoveFriendInteractor(output, session, accounts);

        /** Saves a user with the given friends and pending requests. */
        private void addUser(String name, List<String> friends, List<String> requests) {
            accounts.save(new User(name, "hash", new ArrayList<>(),
                    new ArrayList<>(friends), new ArrayList<>(requests), false));
        }

        /** Creates alice and bob as mutual friends, with alice signed in. */
        private void signedInAsFriends() {
            addUser("alice", List.of("bob"), List.of());
            addUser("bob", List.of("alice"), List.of());
            session.signIn("alice");
        }

        /** Submits the remove-friend request. */
        private void remove(String friendUsername) {
            interactor.execute(new RemoveFriendInputData(friendUsername));
        }
    }

    /** Removing a friend updates both profiles in the repository. */
    @Test
    void removesFriendFromBothProfiles() {
        Fixture fixture = new Fixture();
        fixture.signedInAsFriends();
        fixture.remove("bob");
        assertNull(fixture.output.error);
        assertEquals("alice", fixture.output.success.getUsername());
        assertEquals("bob", fixture.output.success.getFriendUsername());
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
        assertTrue(fixture.accounts.get("bob").getFriends().isEmpty());
    }

    /** The output carries the remaining friends, not the removed one. */
    @Test
    void outputContainsRemainingFriends() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob", "carol"), List.of());
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.addUser("carol", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.remove("bob");
        assertEquals(List.of("carol"), fixture.output.success.getFriends());
    }

    /** Other friendships are not disturbed by a removal. */
    @Test
    void keepsOtherFriendships() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob", "carol"), List.of());
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.addUser("carol", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.remove("bob");
        assertEquals(List.of("carol"), fixture.accounts.get("alice").getFriends());
        assertEquals(List.of("alice"), fixture.accounts.get("carol").getFriends());
    }

    /** Pending friend requests are not touched by a removal. */
    @Test
    void keepsPendingRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of("carol"));
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.addUser("carol", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.remove("bob");
        assertEquals(List.of("carol"), fixture.accounts.get("alice").getFriendRequests());
    }

    /** A one-sided leftover friendship can still be cleaned up. */
    @Test
    void removesOneSidedFriendship() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of());
        fixture.addUser("bob", List.of(), List.of());
        fixture.session.signIn("alice");
        fixture.remove("bob");
        assertNull(fixture.output.error);
        assertTrue(fixture.accounts.get("alice").getFriends().isEmpty());
    }

    /** Someone who is not a friend cannot be removed. */
    @Test
    void rejectsNonFriend() {
        Fixture fixture = new Fixture();
        fixture.signedInAsFriends();
        fixture.addUser("carol", List.of(), List.of());
        fixture.remove("carol");
        assertTrue(fixture.output.error.contains("You can not remove a user you are not friends with"));
        assertNull(fixture.output.success);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriends());
    }

    /** A friend who no longer exists in storage is reported as a failure. */
    @Test
    void reportsFriendWhoseAccountIsGone() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("ghost"), List.of());
        fixture.session.signIn("alice");
        fixture.remove("ghost");
        assertTrue(fixture.output.error.contains("This friend account does not exist"));
        assertNull(fixture.output.success);
    }

    /** Removing a friend always requires an authenticated session. */
    @Test
    void rejectsUnauthenticatedRequests() {
        Fixture fixture = new Fixture();
        fixture.addUser("alice", List.of("bob"), List.of());
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.remove("bob");
        assertTrue(fixture.output.error.contains("sign in"));
        assertNull(fixture.output.success);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriends());
    }

    /** The current account must still exist in storage. */
    @Test
    void rejectsMissingActiveAccount() {
        Fixture fixture = new Fixture();
        fixture.addUser("bob", List.of("alice"), List.of());
        fixture.session.signIn("alice");
        fixture.remove("bob");
        assertEquals("Your account could not be found.", fixture.output.error);
        assertNull(fixture.output.success);
    }

    /** A null username is rejected. */
    @Test
    void rejectsNullUsername() {
        Fixture fixture = new Fixture();
        fixture.signedInAsFriends();
        fixture.remove(null);
        assertTrue(fixture.output.error.contains("Please enter a valid friend username"));
        assertNull(fixture.output.success);
    }

    /** An empty username is rejected. */
    @Test
    void rejectsEmptyUsername() {
        Fixture fixture = new Fixture();
        fixture.signedInAsFriends();
        fixture.remove("");
        assertTrue(fixture.output.error.contains("Please enter a valid friend username"));
        assertNull(fixture.output.success);
    }

    /** A storage failure is reported as a failure and not as a success. */
    @Test
    void reportsStorageFailure() {
        Fixture fixture = new Fixture();
        fixture.signedInAsFriends();
        Output output = new Output();
        RemoveFriendInteractor interactor = new RemoveFriendInteractor(
                output, fixture.session, new FailingWriteAccounts(fixture.accounts));
        interactor.execute(new RemoveFriendInputData("bob"));
        assertTrue(output.error.contains("Could not remove friend"));
        assertNull(output.success);
        assertEquals(List.of("bob"), fixture.accounts.get("alice").getFriends());
    }
}