package interface_adapter.add_friend;

import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import use_case.add_friend.AddFriendOutputBoundary;
import use_case.add_friend.AddFriendOutputData;

/** Shows add-friend results through the profile view model. */
public final class AddFriendPresenter implements AddFriendOutputBoundary {
    private final ProfileViewModel profileViewModel;

    /** Creates the presenter. */
    public AddFriendPresenter(ProfileViewModel profileViewModel) {
        this.profileViewModel = profileViewModel;
    }

    @Override
    public void prepareSuccessView(AddFriendOutputData output, String message) {
        showMessage(message);
    }

    @Override
    public void prepareFailureView(String message) {
        showMessage(message);
    }

    private void showMessage(String message) {
        ProfileState current = profileViewModel.getState();
        profileViewModel.setState(new ProfileState(current.getUsername(), message,
                current.getFriends(), current.getFriendRequests()));
    }
}