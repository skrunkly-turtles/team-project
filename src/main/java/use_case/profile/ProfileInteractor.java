package use_case.profile;

import use_case.session.CurrentUserSession;
import models.User;

/** Selects the authenticated profile without exposing sensitive stored information. */
public final class ProfileInteractor implements ProfileInputBoundary {
  private final CurrentUserSession session;
  private final ProfileUserDataAccessInterface accounts;
  private final ProfileOutputBoundary presenter;

  /** Connects session, account existence and output boundary abstractions. */
  public ProfileInteractor(CurrentUserSession session,
      ProfileUserDataAccessInterface accounts, ProfileOutputBoundary presenter) {
    this.session = session;
    this.accounts = accounts;
    this.presenter = presenter;
  }

  /** Retrieves the current username or declines access for unauthenticated users. */
  @Override
  public void execute() {
    String username = session.getCurrentUsername();
    User user = username == null ? null : accounts.get(username);
    if (user == null) {
      presenter.prepareFailView("Please sign in to view your profile.");
      return;
    }
    presenter.prepareSuccessView(new ProfileOutputData(username, user.getFriends(), user.getFriendRequests()));
  }
}
