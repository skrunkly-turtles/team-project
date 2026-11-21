package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.InMemoryCurrentUserSession;
import data_access.JsonUserDataAccessObject;
import data_access.Pbkdf2PasswordHasher;
import java.nio.file.Path;
import models.CommonUserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.change_password.ChangePasswordInputData;
import use_case.change_password.ChangePasswordInteractor;
import use_case.change_password.ChangePasswordOutputBoundary;
import use_case.change_password.ChangePasswordOutputData;
import use_case.login.LoginInputData;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;
import use_case.logout.LogoutInteractor;
import use_case.profile.ProfileInteractor;
import use_case.profile.ProfileOutputBoundary;
import use_case.profile.ProfileOutputData;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/** Exercises the actual account use cases against JSON persistence. */
class JsonAccountFlowTest {
  @TempDir
  Path directory;

  /** Signup, login, profile, password change, logout and restart use the same JSON file. */
  @Test
  void accountJourneyUsesJsonStorage() {
    Path file = directory.resolve("users.json");
    JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(file);
    Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();

    final SignupOutputData[] registered = new SignupOutputData[1];
    new SignupInteractor(accounts, new SignupOutputBoundary() {
      @Override
      public void prepareSuccessView(SignupOutputData data) {
        registered[0] = data;
      }
      @Override
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, new CommonUserFactory(), hasher).execute(
        new SignupInputData("alice", "oldpassword123", "oldpassword123"));
    assertNotNull(registered[0]);

    JsonUserDataAccessObject reopened = new JsonUserDataAccessObject(file);
    final LoginOutputData[] loggedIn = new LoginOutputData[1];
    new LoginInteractor(reopened, new LoginOutputBoundary() {
      @Override
      public void prepareSuccessView(LoginOutputData data) {
        loggedIn[0] = data;
      }
      @Override
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, hasher, session).execute(new LoginInputData("alice", "oldpassword123"));
    assertEquals("alice", loggedIn[0].getUsername());

    final ProfileOutputData[] profile = new ProfileOutputData[1];
    new ProfileInteractor(session, reopened, new ProfileOutputBoundary() {
      @Override
      public void prepareSuccessView(ProfileOutputData data) {
        profile[0] = data;
      }
      @Override
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }).execute();
    assertEquals("alice", profile[0].getUsername());

    final ChangePasswordOutputData[] updated = new ChangePasswordOutputData[1];
    new ChangePasswordInteractor(reopened, new ChangePasswordOutputBoundary() {
      @Override
      public void prepareSuccessView(ChangePasswordOutputData data) {
        updated[0] = data;
      }
      @Override
      public void prepareFailView(String message) {
        throw new AssertionError(message);
      }
    }, hasher, session).execute(new ChangePasswordInputData(
        "oldpassword123", "newpassword123", "newpassword123"));
    assertNotNull(updated[0]);

    new LogoutInteractor(session, () -> { }).execute();
    assertNull(session.getCurrentUsername());
    String hash = new JsonUserDataAccessObject(file).get("alice").getPasswordHash();
    assertTrue(hasher.matches("newpassword123", hash));
    assertFalse(hasher.matches("oldpassword123", hash));
  }
}
