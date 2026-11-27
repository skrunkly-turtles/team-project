package use_case.delete_pin;

import models.User;

public interface DeletePinUserDataAccessInterface {
    User get(String username);

    /**
     * Removes the user's pin with the given id and saves.
     *
     * @throws IllegalStateException if the account or the pin no longer exists
     */
    void deletePin(String username, int pinId);
}