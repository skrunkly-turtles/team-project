package use_case.remove_friend;

import models.User;
import use_case.add_friend.AddFriendInputData;
import use_case.add_friend.AddFriendOutputData;
import use_case.remove_friend.RemoveFriendInputBoundary;
import use_case.session.CurrentUserSession;

public class RemoveFriendInteractor implements RemoveFriendInputBoundary {
    private final RemoveFriendOutputBoundary presenter;
    private final CurrentUserSession current;
    private final RemoveFriendDataAccessInterface accounts;

    public RemoveFriendInteractor(RemoveFriendOutputBoundary presenter, CurrentUserSession current,
                                  RemoveFriendDataAccessInterface accounts) {
        this.presenter = presenter;
        this.current = current;
        this.accounts = accounts;
    }

    @Override
    public void execute(RemoveFriendInputData input){
        String username = current.getCurrentUsername();
        if(username == null || username.isEmpty()){
            presenter.prepareFailureView("Please sign in before proceeding");
            return;
        }
        if(!accounts.existsByName(username)){
            presenter.prepareFailureView("Your account could not be found.");
            return;
        }
        String friendUsername = input.getFriendUsername();
        if(friendUsername == null || friendUsername.isEmpty()){
            presenter.prepareFailureView("Please enter a valid friend username");
            return;
        }
        if(!accounts.existsByName(friendUsername)){
            presenter.prepareFailureView("This friend account does not exist");
            return;
        }
        if(!accounts.get(username).getFriends().contains(friendUsername)){
            presenter.prepareFailureView("You can not remove a user you are not friends with");
            return;
        }
        try {
            accounts.removeFriend(username, friendUsername);
            presenter.prepareSuccessView(new RemoveFriendOutputData(username, friendUsername, accounts.get(username).getFriends()), "Friend successfully removed");
        } catch (IllegalStateException e){
            presenter.prepareFailureView("Could not remove friend");
        }
    }

}
