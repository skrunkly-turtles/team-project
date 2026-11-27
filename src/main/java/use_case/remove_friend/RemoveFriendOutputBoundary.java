package use_case.remove_friend;

import use_case.remove_friend.RemoveFriendOutputData;

public interface RemoveFriendOutputBoundary {

    void prepareSuccessView(RemoveFriendOutputData output, String message);
    void prepareFailureView(String message);

}
