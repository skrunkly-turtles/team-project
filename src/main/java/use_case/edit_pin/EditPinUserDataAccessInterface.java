package use_case.edit_pin;

import models.Pin;
import models.User;

public interface EditPinUserDataAccessInterface {
    User get(String username);

    /**
     * Replaces the user's pin that has the same id as the given pin, and saves.
     *
     * @throws IllegalStateException if the account or the pin no longer exists
     */
    void updatePin(String username, Pin pin);
}