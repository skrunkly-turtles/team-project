package use_case.respond_friend;

/** Identifies a pending request and whether the signed-in user accepts it. */
public final class RespondFriendInputData {
    private final String requesterUsername;
    private final boolean accept;

    /** Creates a response without including an untrusted responding username. */
    public RespondFriendInputData(String requesterUsername, boolean accept) {
        this.requesterUsername = requesterUsername;
        this.accept = accept;
    }

    /** Returns the username of the user who sent the request. */
    public String getRequesterUsername() {
        return requesterUsername;
    }

    /** Returns true to accept the request, false to decline it. */
    public boolean isAccept() {
        return accept;
    }
}