package interface_adapter.add_friend;

import use_case.add_friend.AddFriendInputBoundary;
import use_case.add_friend.AddFriendInputData;

/** Converts the widget's typed username into an add-friend request. */
public final class AddFriendController {
    private final AddFriendInputBoundary interactor;

    /** Creates the controller. */
    public AddFriendController(AddFriendInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Requests that the signed-in user add the given friend. */
    public void execute(String friendUsername) {
        interactor.execute(new AddFriendInputData(friendUsername));
    }
}