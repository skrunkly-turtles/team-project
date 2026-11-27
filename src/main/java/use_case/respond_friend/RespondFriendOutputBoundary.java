package use_case.respond_friend;

/** Receives the outcome of the respond-to-request use case. */
public interface RespondFriendOutputBoundary {
    /** Presents a successful accept or decline. */
    void prepareSuccessView(RespondFriendOutputData output, String message);

    /** Presents a failed response. */
    void prepareFailureView(String message);
}