package interface_adapter.map;

import use_case.display_map.DisplayMapOutputBoundary;
import use_case.display_map.DisplayMapOutputData;

/** Converts use-case output into observable map display state. */
public class MapPresenter implements DisplayMapOutputBoundary {
    private final MapViewModel viewModel;

    /**
     * Creates the presenter for map display updates.
     * @param viewModel observable display state
     */
    public MapPresenter(MapViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Updates the view model, which notifies the view.
     * @param outputData initial geographic viewport
     */
    @Override
    public void present(DisplayMapOutputData outputData) {
        viewModel.setViewport(outputData.getLatitude(), outputData.getLongitude(), outputData.getZoom());
    }
}
