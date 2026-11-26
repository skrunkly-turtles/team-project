package use_case.online;

import data_access.supabase.SupabaseUserDataAccessObject;
import use_case.add_friend.AddFriendInputBoundary;
import use_case.add_friend.AddFriendInputData;
import use_case.add_friend.AddFriendInteractor;
import use_case.add_friend.AddFriendOutputBoundary;
import use_case.add_friend.AddFriendOutputData;
import use_case.session.CurrentUserSession;

/** Runs the existing add-friend interactor off the Swing thread. */
public final class OnlineAddFriendInteractor implements AddFriendInputBoundary {
  private final SupabaseUserDataAccessObject accounts;
  private final CurrentUserSession session;
  private final AddFriendOutputBoundary presenter;

  public OnlineAddFriendInteractor(SupabaseUserDataAccessObject accounts,
      CurrentUserSession session, AddFriendOutputBoundary presenter) {
    this.accounts = accounts;
    this.session = session;
    this.presenter = presenter;
  }

  @Override
  public void execute(AddFriendInputData input) {
    OnlineUi.background(() -> {
      try {
        AddFriendOutputBoundary uiOutput = new AddFriendOutputBoundary() {
          @Override
          public void prepareSuccessView(AddFriendOutputData output, String message) {
            OnlineUi.present(() -> presenter.prepareSuccessView(output, message));
          }
          @Override
          public void prepareFailureView(String message) {
            OnlineUi.present(() -> presenter.prepareFailureView(message));
          }
        };
        new AddFriendInteractor(accounts, uiOutput, session).execute(input);
      } catch (RuntimeException exception) {
        OnlineUi.present(() -> presenter.prepareFailureView(
            "Could not contact the online friend service."));
      }
    });
  }
}
