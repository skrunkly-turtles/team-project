package interface_adapter.edit_pin;

import use_case.edit_pin.EditPinOutputBoundary;
import use_case.edit_pin.EditPinOutputData;

/** Formats edit-pin results for the view model. */
public final class EditPinPresenter implements EditPinOutputBoundary {
    private final EditPinViewModel viewModel;

    public EditPinPresenter(EditPinViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(EditPinOutputData output, String message) {
        viewModel.setState(new EditPinState(message, true));
    }

    @Override
    public void prepareFailureView(String message) {
        viewModel.setState(new EditPinState(message, false));
    }
}