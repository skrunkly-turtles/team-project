package use_case.signup;

import models.User;

/** Storage operations needed by signup, not tied to any particular database. */
public interface SignupUserDataAccessInterface {
    /** Checks for an existing username. */
    boolean existsByName(String username);

    /** Saves a newly created user or throws if the username already exists. */
    void save(User user);
}
