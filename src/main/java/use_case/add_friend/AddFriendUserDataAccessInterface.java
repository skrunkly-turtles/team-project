package use_case.add_friend;

import models.User;

public interface AddFriendUserDataAccessInterface {
    /* Returns whether a User exists */
    boolean existsByName(String username);

    /* Returns the account by the username */
    User get(String username);

    /**
     * Records a pending request from one user to another.
     *
     * @throws IllegalStateException if either account is missing, they are already friends,
     *     or a request is already pending
     */
    void sendFriendRequest(String username, String friendUsername);
}
