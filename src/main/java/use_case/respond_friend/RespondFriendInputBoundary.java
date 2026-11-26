package use_case.respond_friend;

/** Defines the action of accepting or declining a pending friend request. */
public interface RespondFriendInputBoundary {
    /** Responds to the request that the given user sent to the signed-in user. */
    void execute(RespondFriendInputData input);
}