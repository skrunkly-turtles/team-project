package use_case.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import data_access.InMemoryCurrentUserSession;
import data_access.InMemoryUserDataAccessObject;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;

/** Verifies that the general profile is restricted to the authenticated user. */
class ProfileInteractorTest {
  /** Fake presenter for observing the profile use case without Swing. */
  private static final class Output implements ProfileOutputBoundary {
    private ProfileOutputData success;
    private String failure;

    /** Records the profile data. */
    @Override
    public void prepareSuccessView(ProfileOutputData data) {
      success = data;
    }

    /** Records an authentication error. */
    @Override
    public void prepareFailView(String message) {
      failure = message;
    }
  }

  /** A signed-in existing user can view only their own profile. */
  @Test
  void displaysAuthenticatedProfile() {
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    session.signIn("alice");
    InMemoryUserDataAccessObject accounts = new InMemoryUserDataAccessObject();
    accounts.save(new CommonUserFactory().create("alice", "hash"));
    Output output = new Output();
    new ProfileInteractor(session, accounts, output).execute();
    assertEquals("alice", output.success.getUsername());
    assertNull(output.failure);
  }

  /** Users who have not logged in cannot retrieve profile information. */
  @Test
  void refusesWhenSignedOut() {
    Output output = new Output();
    new ProfileInteractor(new InMemoryCurrentUserSession(),
        new InMemoryUserDataAccessObject(), output).execute();
    assertNotNull(output.failure);
    assertNull(output.success);
  }

  /** A stale session without a registered account does not provide profile access. */
  @Test
  void refusesMissingAccount() {
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    session.signIn("missing");
    Output output = new Output();
    new ProfileInteractor(session, new InMemoryUserDataAccessObject(), output).execute();
    assertNotNull(output.failure);
    assertNull(output.success);
  }
}
