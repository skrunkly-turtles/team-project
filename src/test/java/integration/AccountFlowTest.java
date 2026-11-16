package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.FileUserDataAccessObject;
import data_access.InMemoryCurrentUserSession;
import data_access.Pbkdf2PasswordHasher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;
import use_case.change_password.ChangePasswordInputData;
import use_case.change_password.ChangePasswordInteractor;
import use_case.change_password.ChangePasswordOutputData;
import use_case.login.LoginInputData;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputData;
import use_case.logout.LogoutInteractor;
import use_case.profile.ProfileInteractor;
import use_case.profile.ProfileOutputData;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputData;

/** End-to-end use case integration with a real local account store but no Swing UI. */
class AccountFlowTest {
  /** Exercises signup, login, profile, password change, logout, and restart. */
  @Test
  void entireAccountJourneySurvivesRestart() throws IOException {
    Path file = Files.createTempDirectory("account-journey-").resolve("accounts.properties");
    FileUserDataAccessObject accounts = new FileUserDataAccessObject(file);
    Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    final SignupOutputData[] registered = new SignupOutputData[1];
    new SignupInteractor(accounts, new use_case.signup.SignupOutputBoundary() {
      /** Records successful signup. */
      public void prepareSuccessView(SignupOutputData value) {
        registered[0] = value;
      }
      /** Aborts this successful scenario on unexpected failure. */
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, new CommonUserFactory(), hasher).execute(
        new SignupInputData("alice", "oldpassword123", "oldpassword123"));
    assertNotNull(registered[0]);
    assertFalse(Files.readString(file).contains("oldpassword123"));

    FileUserDataAccessObject reopened = new FileUserDataAccessObject(file);
    final LoginOutputData[] loggedIn = new LoginOutputData[1];
    new LoginInteractor(reopened, new use_case.login.LoginOutputBoundary() {
      /** Records authentication success. */
      public void prepareSuccessView(LoginOutputData data) {
        loggedIn[0] = data;
      }
      /** Aborts this successful scenario on unexpected failure. */
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, hasher, session).execute(new LoginInputData("alice", "oldpassword123"));
    assertEquals("alice", loggedIn[0].getUsername());

    final ProfileOutputData[] profile = new ProfileOutputData[1];
    new ProfileInteractor(session, reopened, new use_case.profile.ProfileOutputBoundary() {
      /** Records returned profile information. */
      public void prepareSuccessView(ProfileOutputData output) {
        profile[0] = output;
      }
      /** Aborts this successful scenario on unexpected failure. */
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }).execute();
    assertEquals("alice", profile[0].getUsername());

    final ChangePasswordOutputData[] updated = new ChangePasswordOutputData[1];
    new ChangePasswordInteractor(reopened, new use_case.change_password.ChangePasswordOutputBoundary() {
      /** Records the password-change success. */
      public void prepareSuccessView(ChangePasswordOutputData data) {
        updated[0] = data;
      }
      /** Aborts this successful scenario on unexpected failure. */
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, hasher, session).execute(new ChangePasswordInputData(
        "oldpassword123", "newpassword123", "newpassword123"));
    assertEquals("alice", updated[0].getUsername());

    new LogoutInteractor(session, () -> { }).execute();
    assertNull(session.getCurrentUsername());
    FileUserDataAccessObject afterRestart = new FileUserDataAccessObject(file);
    assertTrue(hasher.matches("newpassword123",
        afterRestart.get("alice").getPasswordHash()));
    assertFalse(hasher.matches("oldpassword123",
        afterRestart.get("alice").getPasswordHash()));
  }
}
