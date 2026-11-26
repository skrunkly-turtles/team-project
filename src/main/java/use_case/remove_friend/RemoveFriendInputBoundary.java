package use_case.remove_friend;

import use_case.add_friend.AddFriendInputData;

public interface RemoveFriendInputBoundary {
    void execute(RemoveFriendInputData input);
}
