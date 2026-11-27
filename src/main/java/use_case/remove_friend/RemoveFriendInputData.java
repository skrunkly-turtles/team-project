package use_case.remove_friend;

public class RemoveFriendInputData {
    private final String friendUsername;
    public RemoveFriendInputData(String friendUsername) {

        this.friendUsername = friendUsername;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
}
