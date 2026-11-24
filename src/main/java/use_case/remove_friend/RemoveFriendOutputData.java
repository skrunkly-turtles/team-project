package use_case.remove_friend;

import java.util.List;

public class RemoveFriendOutputData {
    private final String username;
    private final String friendUsername;
    private final List<String> friends;

    public RemoveFriendOutputData(String username, String friendUsername,  List<String> friends) {
        this.username = username;
        this.friendUsername = friendUsername;
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public List<String> getFriends() {
        return friends;
    }
}
