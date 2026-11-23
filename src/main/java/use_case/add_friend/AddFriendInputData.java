package use_case.add_friend;

public class AddFriendInputData {

    private final String friendUsername;
    public AddFriendInputData(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
}
