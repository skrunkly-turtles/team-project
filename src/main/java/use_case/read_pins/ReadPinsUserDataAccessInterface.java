package use_case.read_pins;

import java.util.List;
import models.User;

/** Storage operations required by the read-pins use case. */
public interface ReadPinsUserDataAccessInterface {
    /** Returns the account associated with a username, or null if none exists. */
    User get(String username);

    /** Returns independent copies of every account. */
    List<User> getAll();
}