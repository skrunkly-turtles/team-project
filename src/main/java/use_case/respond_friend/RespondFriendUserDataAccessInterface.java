package use_case.respond_friend;

import models.User;

/** Storage operations required by the respond-to-request use case. */
public interface RespondFriendUserDataAccessInterface {
    /** Returns the account associated with a username, or null if none exists. */
    User get(String username);

    /**
     * Makes two users friends and clears the request, as a single operation.
     *
     * @throws IllegalStateException if either account is missing, there is no pending
     *     request, or they are already friends
     */
    void acceptFriendRequest(String username, String requesterUsername);

    /**
     * Removes a pending request without creating a friendship.
     *
     * @throws IllegalStateException if the account is missing or there is no pending request
     */
    void declineFriendRequest(String username, String requesterUsername);
}