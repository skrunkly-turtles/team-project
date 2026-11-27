package interface_adapter.remove_friend;

import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import use_case.remove_friend.RemoveFriendOutputBoundary;
import use_case.remove_friend.RemoveFriendOutputData;

/** Shows remove-friend results through the profile view model. */
public final class RemoveFriendPresenter implements RemoveFriendOutputBoundary {
    private final ProfileViewModel profileViewModel;

    /** Creates the presenter. */
    public RemoveFriendPresenter(ProfileViewModel profileViewModel) {
        this.profileViewModel = profileViewModel;
    }

    @Override
    public void prepareSuccessView(RemoveFriendOutputData output, String message) {
        ProfileState current = profileViewModel.getState();
        profileViewModel.setState(new ProfileState(output.getUsername(), message,
                output.getFriends(), current.getFriendRequests()));
    }

    @Override
    public void prepareFailureView(String message) {
        ProfileState current = profileViewModel.getState();
        profileViewModel.setState(new ProfileState(current.getUsername(), message,
                current.getFriends(), current.getFriendRequests()));
    }
}