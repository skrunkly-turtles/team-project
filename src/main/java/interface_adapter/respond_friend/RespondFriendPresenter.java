package interface_adapter.respond_friend;

import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import use_case.respond_friend.RespondFriendOutputBoundary;
import use_case.respond_friend.RespondFriendOutputData;

/** Shows respond-to-request results through the profile view model. */
public final class RespondFriendPresenter
        implements RespondFriendOutputBoundary {
    private final ProfileViewModel profileViewModel;

    /** Creates the presenter. */
    public RespondFriendPresenter(ProfileViewModel profileViewModel) {
        this.profileViewModel = profileViewModel;
    }

    @Override
    public void prepareSuccessView(RespondFriendOutputData output, String message) {
        profileViewModel.setState(new ProfileState(output.getUsername(), message,
                output.getFriends(), output.getFriendRequests()));
    }

    @Override
    public void prepareFailureView(String message) {
        ProfileState current = profileViewModel.getState();
        profileViewModel.setState(new ProfileState(current.getUsername(), message,
                current.getFriends(), current.getFriendRequests()));
    }
}