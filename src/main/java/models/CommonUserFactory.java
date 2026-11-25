package models;

import java.util.ArrayList;

/** Default implementation used by the signup use case. */
public class CommonUserFactory implements models.UserFactory {
    /** Creates an initially empty account. */
    @Override
    public User create(String username, String passwordHash) {
        return new User(username, passwordHash, new ArrayList<>(), new ArrayList<>());
    }
}
