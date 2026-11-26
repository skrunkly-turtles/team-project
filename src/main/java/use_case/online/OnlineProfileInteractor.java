package use_case.online;

import use_case.profile.ProfileInputBoundary;
import use_case.profile.ProfileInteractor;
import use_case.profile.ProfileOutputBoundary;
import use_case.profile.ProfileOutputData;
import use_case.profile.ProfileUserDataAccessInterface;
import use_case.session.CurrentUserSession;

/** Uses the existing profile business logic with non-blocking online data access. */
public final class OnlineProfileInteractor implements ProfileInputBoundary {
  private final CurrentUserSession session;
  private final ProfileUserDataAccessInterface accounts;
  private final ProfileOutputBoundary presenter;

  public OnlineProfileInteractor(CurrentUserSession session,
      ProfileUserDataAccessInterface accounts, ProfileOutputBoundary presenter) {
    this.session = session;
    this.accounts = accounts;
    this.presenter = presenter;
  }

  @Override
  public void execute() {
    OnlineUi.background(() -> {
      try {
        new ProfileInteractor(session, accounts, new ProfileOutputBoundary() {
          @Override
          public void prepareSuccessView(ProfileOutputData output) {
            OnlineUi.present(() -> presenter.prepareSuccessView(output));
          }
          @Override
          public void prepareFailView(String message) {
            OnlineUi.present(() -> presenter.prepareFailView(message));
          }
        }).execute();
      } catch (RuntimeException exception) {
        OnlineUi.present(() -> presenter.prepareFailView("Could not load online profile."));
      }
    });
  }
}
