package use_case.remove_friend;

import models.User;

public interface RemoveFriendDataAccessInterface {
    boolean existsByName(String username);
    User get(String username);

    /**
     * Removes the connection between one user and another in a single operation
     *
     * @throws IllegalStateException if either account is missing or they are not friends,
     */
    void removeFriend(String username, String friendUsername);
}
