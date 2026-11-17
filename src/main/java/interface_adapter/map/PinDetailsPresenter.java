package interface_adapter.map;

import use_case.pin_details.PinDetailsOutputBoundary;
import use_case.pin_details.PinDetailsOutputData;

/** Updates the observable details state. */
public class PinDetailsPresenter implements PinDetailsOutputBoundary {
    private final PinDetailsViewModel viewModel;
    /**
     * Connects the presenter to display state.
     * @param viewModel draft display state
     */
    public PinDetailsPresenter(PinDetailsViewModel viewModel) { this.viewModel = viewModel; }
    /**
     * Publishes a details result for the form and navigation observers.
     * @param result current draft and stage
     */
    @Override
    public void present(PinDetailsOutputData result) { viewModel.setState(result); }
}
