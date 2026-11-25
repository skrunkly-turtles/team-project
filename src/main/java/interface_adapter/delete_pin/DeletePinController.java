package interface_adapter.delete_pin;

import use_case.delete_pin.DeletePinInputBoundary;
import use_case.delete_pin.DeletePinInputData;

/** Converts a selected pin into a delete-pin request. */
public final class DeletePinController {
    private final DeletePinInputBoundary interactor;

    public DeletePinController(DeletePinInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Deletes the pin with the given id. */
    public void execute(int pinId) {
        interactor.execute(new DeletePinInputData(pinId));
    }
}