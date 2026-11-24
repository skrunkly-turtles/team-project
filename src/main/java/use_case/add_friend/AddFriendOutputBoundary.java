package use_case.add_friend;

public interface AddFriendOutputBoundary {
    void prepareSuccessView(AddFriendOutputData output, String message);
    void prepareFailureView(String message);
}
