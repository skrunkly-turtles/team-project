package use_case.online;

import data_access.supabase.SupabaseAuthClient;
import java.util.regex.Pattern;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/** Online signup uses Supabase Auth, not local password hashing. */
public final class OnlineSignupInteractor implements SignupInputBoundary {
  private static final Pattern USERNAME = Pattern.compile("[A-Za-z][A-Za-z0-9_]{2,23}");
  private final SupabaseAuthClient auth;
  private final SignupOutputBoundary presenter;

  public OnlineSignupInteractor(SupabaseAuthClient auth, SignupOutputBoundary presenter) {
    this.auth = auth;
    this.presenter = presenter;
  }

  @Override
  public void execute(SignupInputData input) {
    String username = input.getUsername();
    String email = input.getEmail();
    String password = input.getPassword();
    if (username == null || !USERNAME.matcher(username).matches()) {
      presenter.prepareFailView("Username must be 3-24 letters, numbers, or underscores.");
    } else if (email == null || !email.contains("@") || !email.contains(".")) {
      presenter.prepareFailView("Enter a valid email address.");
    } else if (password == null || password.length() < 8) {
      presenter.prepareFailView("Password must contain at least 8 characters.");
    } else if (!password.equals(input.getRepeatedPassword())) {
      presenter.prepareFailView("Passwords do not match.");
    } else {
      OnlineUi.background(() -> {
        try {
          auth.signUp(username, email, password);
          OnlineUi.present(() -> presenter.prepareSuccessView(new SignupOutputData(username)));
        } catch (RuntimeException exception) {
          OnlineUi.present(() -> presenter.prepareFailView(
              "Could not create online account. Username/email may already be in use."));
        }
      });
    }
  }
}
