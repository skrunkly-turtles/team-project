package interface_adapter.delete_pin;

import interface_adapter.ViewModel;

/** Observable delete-pin data read by the pins view. */
public final class DeletePinViewModel extends ViewModel<DeletePinState> {
    public DeletePinViewModel() {
        super(new DeletePinState());
    }
}