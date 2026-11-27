package interface_adapter.edit_pin;

import use_case.edit_pin.EditPinInputBoundary;
import use_case.edit_pin.EditPinInputData;

/** Converts the edit form into an edit-pin request. */
public final class EditPinController {
    private final EditPinInputBoundary interactor;

    public EditPinController(EditPinInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Submits the edited contents of the given pin. */
    public void execute(int pinId, String name, String description, String latitude,
                        String longitude, String passageText, String bookTitle, String author, String year) {
        interactor.execute(new EditPinInputData(pinId, description, latitude, longitude,
                passageText, bookTitle, author, year));
    }
}