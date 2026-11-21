package use_case.change_password;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import data_access.Pbkdf2PasswordHasher;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;

/** Checks authentication, validation, and persistence for the Change Password use case. */
class ChangePasswordInteractorTest {
  /** Test double for presenter notifications. */
  private static final class Output implements ChangePasswordOutputBoundary {
    private String error;
    private ChangePasswordOutputData success;

    /** Records the use case's success data. */
    @Override
    public void prepareSuccessView(ChangePasswordOutputData output) {
      success = output;
    }

    /** Records the use case's failure message. */
    @Override
    public void prepareFailView(String message) {
      error = message;
    }
  }

  /** Contains a complete in-memory test fixture. */
  private static final class Fixture {
    private final InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
    private final InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    private final Output output = new Output();
    private final ChangePasswordInteractor interactor = new ChangePasswordInteractor(
        accounts, output, hasher, session);

    /** Initializes an existing authenticated account with a known password. */
    private void signedIn() {
      accounts.save(new CommonUserFactory().create("alice", hasher.hash("oldpassword")));
      session.signIn("alice");
    }

    /** Submits the password-change request. */
    private void change(String oldValue, String newValue, String repeated) {
      interactor.execute(new ChangePasswordInputData(oldValue, newValue, repeated));
    }
  }

  /** Changes the stored hash without exposing the replacement password to the presenter. */
  @Test
  void changesPasswordCorrectly() {
    Fixture fixture = new Fixture();
    fixture.signedIn();
    fixture.change("oldpassword", "newpassword123", "newpassword123");
    assertEquals("alice", fixture.output.success.getUsername());
    String hash = fixture.accounts.get("alice").getPasswordHash();
    assertTrue(fixture.hasher.matches("newpassword123", hash));
    assertFalse(fixture.hasher.matches("oldpassword", hash));
    assertEquals("alice", fixture.session.getCurrentUsername());
  }

  /** A password change always requires an authenticated session. */
  @Test
  void rejectsUnauthenticatedRequests() {
    Fixture fixture = new Fixture();
    fixture.change("oldpassword", "newpassword123", "newpassword123");
    assertNotNull(fixture.output.error);
    assertNull(fixture.output.success);
  }

  /** The current account must still exist in storage. */
  @Test
  void rejectsMissingActiveAccount() {
    Fixture fixture = new Fixture();
    fixture.session.signIn("alice");
    fixture.change("oldpassword", "newpassword123", "newpassword123");
    assertTrue(fixture.output.error.contains("could not be found"));
  }

  /** The replacement cannot be authorized with an incorrect current password. */
  @Test
  void rejectsWrongOldPassword() {
    Fixture fixture = new Fixture();
    fixture.signedIn();
    fixture.change("incorrect", "newpassword123", "newpassword123");
    assertTrue(fixture.output.error.contains("incorrect"));
    assertNull(fixture.output.success);
  }

  /** A replacement shorter than eight characters is rejected. */
  @Test
  void rejectsShortReplacement() {
    Fixture fixture = new Fixture();
    fixture.signedIn();
    fixture.change("oldpassword", "short", "short");
    assertTrue(fixture.output.error.contains("8 characters"));
  }

  /** Both new-password entries must agree. */
  @Test
  void rejectsMismatchedConfirmation() {
    Fixture fixture = new Fixture();
    fixture.signedIn();
    fixture.change("oldpassword", "newpassword123", "different456");
    assertTrue(fixture.output.error.contains("do not match"));
  }

  /** An unchanged password is not accepted as a password update. */
  @Test
  void rejectsSamePassword() {
    Fixture fixture = new Fixture();
    fixture.signedIn();
    fixture.change("oldpassword", "oldpassword", "oldpassword");
    assertTrue(fixture.output.error.contains("different"));
  }
}
