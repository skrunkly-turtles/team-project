package use_case.add_friend;

import models.User;
import use_case.add_friend.AddFriendInputBoundary;
import use_case.session.CurrentUserSession;

/** Implements current-password verification and secure replacement of account credentials. */

public final class AddFriendInteractor implements AddFriendInputBoundary{
    private final AddFriendOutputBoundary presenter;
    private final CurrentUserSession session;
    private final AddFriendUserDataAccessInterface accounts;
    public AddFriendInteractor(AddFriendUserDataAccessInterface accounts, AddFriendOutputBoundary presenter,
                               CurrentUserSession session) {
        this.presenter = presenter;
        this.session = session;
        this.accounts = accounts;
    }
    @Override
    public void execute(AddFriendInputData input) {
        String username = session.getCurrentUsername();
        if (username == null || username.isEmpty()){
            presenter.prepareFailureView("Please sign in");
            return;
        }
        String friendUsername = input.getFriendUsername();
        if (friendUsername == null || friendUsername.isEmpty()) {
            presenter.prepareFailureView("Please enter a friend name");
            return;
        }
        if (friendUsername.equals(username)) {
            presenter.prepareFailureView("You can not add yourself as a friend");
            return;
        }
        User account = accounts.get(username);
        User friendAccount = accounts.get(friendUsername);
        if (account == null || friendAccount == null) {
            presenter.prepareFailureView("Account does not exist");
            return;
        }
        User friend = accounts.get(friendUsername);
        if (friend == null) {
            presenter.prepareFailureView("No user named " + friendUsername + " exists.");
            return;
        }
        if (account.getFriends().contains(friendUsername)) {
            presenter.prepareFailureView("You are already friends with " + friendUsername + ".");
            return;
        }
        if (friend.getFriendRequests().contains(username)) {
            presenter.prepareFailureView("You already sent " + friendUsername + " a request.");
            return;
        }
        if (account.getFriendRequests().contains(friendUsername)) {
            presenter.prepareFailureView(friendUsername + " already sent you a request. Accept it instead.");
            return;
        }
        try {
            accounts.sendFriendRequest(username, friendUsername);
            presenter.prepareSuccessView(new AddFriendOutputData(username, friendUsername),
                    "Friend request sent to " + friendUsername + ".");
        } catch (IllegalStateException exception) {
            presenter.prepareFailureView("Could not send request. Please try again.");
        }
    }
}
