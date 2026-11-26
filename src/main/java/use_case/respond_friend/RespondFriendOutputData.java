package use_case.respond_friend;

import java.util.ArrayList;
import java.util.List;

/** The signed-in user's updated lists after responding to a request. */
public final class RespondFriendOutputData {
    private final String username;
    private final String requesterUsername;
    private final boolean accepted;
    private final List<String> friends;
    private final List<String> friendRequests;

    /** Records the response and the user's updated friends and remaining requests. */
    public RespondFriendOutputData(String username, String requesterUsername,
                                          boolean accepted, List<String> friends, List<String> friendRequests) {
        this.username = username;
        this.requesterUsername = requesterUsername;
        this.accepted = accepted;
        this.friends = new ArrayList<>(friends);
        this.friendRequests = new ArrayList<>(friendRequests);
    }

    /** Returns the signed-in user's username. */
    public String getUsername() {
        return username;
    }

    /** Returns the username of the user who sent the request. */
    public String getRequesterUsername() {
        return requesterUsername;
    }

    /** Returns true if the request was accepted, false if declined. */
    public boolean isAccepted() {
        return accepted;
    }

    /** Returns a copy of the user's friends after responding. */
    public List<String> getFriends() {
        return new ArrayList<>(friends);
    }

    /** Returns a copy of the user's remaining pending requests. */
    public List<String> getFriendRequests() {
        return new ArrayList<>(friendRequests);
    }
}