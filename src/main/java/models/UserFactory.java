package models;

/** Creates new user entities independently of the signup use case. */
public interface UserFactory {
    /** Creates an account with no pins or friends. */
    User create(String username, String passwordHash);
}
