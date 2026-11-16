package interface_adapter.map;

import java.util.function.Consumer;
import use_case.select_location.SelectedLocation;
import use_case.select_location.SelectLocationOutputBoundary;
import use_case.select_location.SelectLocationOutputData;
import use_case.select_location.SelectLocationOutputData.Stage;

/** Updates selection display state and hands confirmed coordinates to navigation. */
public class LocationSelectionPresenter implements SelectLocationOutputBoundary {
    private final LocationSelectionViewModel viewModel;
    private final Consumer<SelectedLocation> onContinue;

    /**
     * Connects selection presentation and the next creation step.
     * @param viewModel state observed by the map view
     * @param onContinue navigation callback retaining the immutable location
     */
    public LocationSelectionPresenter(LocationSelectionViewModel viewModel, Consumer<SelectedLocation> onContinue) {
        this.viewModel = viewModel;
        this.onContinue = onContinue;
    }

    /**
     * Publishes state before handing a confirmed location to the next step.
     * @param result latest selection result
     */
    @Override
    public void present(SelectLocationOutputData result) {
        viewModel.setState(result);
        if (result.getStage() == Stage.CONFIRMED) {
            onContinue.accept(result.getLocation());
        }
    }
}
