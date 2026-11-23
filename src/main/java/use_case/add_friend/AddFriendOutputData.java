package use_case.add_friend;

/** The non-sensitive result of a successfully sent friend request. */
public final class AddFriendOutputData {
    private final String username;
    private final String friendUsername;

    /** Records which user sent a request to which other user. */
    public AddFriendOutputData(String username, String friendUsername) {
        this.username = username;
        this.friendUsername = friendUsername;
    }

    /** Returns the username of the user who sent the request. */
    public String getUsername() {
        return username;
    }

    /** Returns the username of the user who received the request. */
    public String getFriendUsername() {
        return friendUsername;
    }
}