package interface_adapter.read_pins;

import java.util.ArrayList;
import use_case.read_pins.ReadPinsOutputBoundary;
import use_case.read_pins.ReadPinsOutputData;

/** Formats read-pins results for the view model. */
public final class ReadPinsPresenter implements ReadPinsOutputBoundary {
    private final ReadPinsViewModel viewModel;

    /** Creates the presenter. */
    public ReadPinsPresenter(ReadPinsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(ReadPinsOutputData output) {
        viewModel.setState(new ReadPinsState(output.getPins(), ""));
    }

    @Override
    public void prepareFailureView(String message) {
        viewModel.setState(new ReadPinsState(new ArrayList<>(), message));
    }
}