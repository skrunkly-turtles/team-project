package interface_adapter.add_friend;

import interface_adapter.ViewModel;

/** Observable add-friend data read by the add-friend Swing widget. */
public final class AddFriendViewModel extends ViewModel<AddFriendState> {
    /** Creates an empty add-friend ViewModel. */
    public AddFriendViewModel() {
        super(new AddFriendState());
    }
}
