package use_case.login;

import models.User;

/** Storage operations required for authentication. */
public interface LoginUserDataAccessInterface {
    /** Returns the registered user, or null if absent. */
    User get(String username);
}
