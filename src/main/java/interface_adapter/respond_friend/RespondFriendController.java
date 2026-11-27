package interface_adapter.respond_friend;

import use_case.respond_friend.RespondFriendInputBoundary;
import use_case.respond_friend.RespondFriendInputData;

/** Converts a selected request and a decision into a respond-to-request call. */
public final class RespondFriendController {
    private final RespondFriendInputBoundary interactor;

    /** Creates the controller. */
    public RespondFriendController(RespondFriendInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Accepts the request from the given user. */
    public void accept(String requesterUsername) {
        interactor.execute(new RespondFriendInputData(requesterUsername, true));
    }

    /** Declines the request from the given user. */
    public void decline(String requesterUsername) {
        interactor.execute(new RespondFriendInputData(requesterUsername, false));
    }
}