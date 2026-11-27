package interface_adapter.delete_pin;

import use_case.delete_pin.DeletePinOutputBoundary;
import use_case.delete_pin.DeletePinOutputData;

/** Formats delete-pin results for the view model. */
public final class DeletePinPresenter implements DeletePinOutputBoundary {
    private final DeletePinViewModel viewModel;

    public DeletePinPresenter(DeletePinViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(DeletePinOutputData output, String message) {
        viewModel.setState(new DeletePinState(message, true));
    }

    @Override
    public void prepareFailureView(String message) {
        viewModel.setState(new DeletePinState(message, false));
    }
}