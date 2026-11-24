package use_case.respond_friend;

import models.User;
import use_case.session.CurrentUserSession;

/** Accepts or declines a pending friend request for the signed-in user. */
public final class RespondFriendInteractor
        implements RespondFriendInputBoundary {
    private final RespondFriendUserDataAccessInterface accounts;
    private final RespondFriendOutputBoundary presenter;
    private final CurrentUserSession session;

    /** Creates the interactor with only inward-facing interfaces. */
    public RespondFriendInteractor(RespondFriendUserDataAccessInterface accounts,
                                          RespondFriendOutputBoundary presenter, CurrentUserSession session) {
        this.accounts = accounts;
        this.presenter = presenter;
        this.session = session;
    }

    /** Validates the request, then accepts or declines it. */
    @Override
    public void execute(RespondFriendInputData input) {
        String username = session.getCurrentUsername();
        if (username == null) {
            presenter.prepareFailureView("Please sign in to respond to a friend request.");
            return;
        }
        String requester = input.getRequesterUsername();
        if (requester == null || requester.isEmpty()) {
            presenter.prepareFailureView("Please select a request.");
            return;
        }
        User current = accounts.get(username);
        if (current == null) {
            presenter.prepareFailureView("Your account could not be found.");
            return;
        }
        if (!current.getFriendRequests().contains(requester)) {
            presenter.prepareFailureView("No pending request from " + requester + ".");
            return;
        }
        if (input.isAccept() && current.getFriends().contains(requester)) {
            presenter.prepareFailureView("You are already friends with " + requester + ".");
            return;
        }
        try {
            if (input.isAccept()) {
                accounts.acceptFriendRequest(username, requester);
            } else {
                accounts.declineFriendRequest(username, requester);
            }
            User updated = accounts.get(username);
            String message = input.isAccept()
                    ? "You are now friends with " + requester + "."
                    : "Declined the request from " + requester + ".";
            presenter.prepareSuccessView(new RespondFriendOutputData(username, requester,
                    input.isAccept(), updated.getFriends(), updated.getFriendRequests()), message);
        } catch (IllegalStateException exception) {
            presenter.prepareFailureView("Could not respond to request. Please try again.");
        }
    }
}