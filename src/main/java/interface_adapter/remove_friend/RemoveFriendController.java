package interface_adapter.remove_friend;

import use_case.remove_friend.RemoveFriendInputBoundary;
import use_case.remove_friend.RemoveFriendInputData;

/** Converts a selected friend into a remove-friend call. */
public final class RemoveFriendController {
    private final RemoveFriendInputBoundary interactor;

    /** Creates the controller. */
    public RemoveFriendController(RemoveFriendInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Removes the given friend. */
    public void execute(String friendUsername) {
        interactor.execute(new RemoveFriendInputData(friendUsername));
    }
}