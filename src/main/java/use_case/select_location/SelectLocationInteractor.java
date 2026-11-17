package use_case.select_location;

import use_case.select_location.SelectLocationOutputData.Stage;

/** Manages an unsaved location draft independently of the map widget. */
public class SelectLocationInteractor implements SelectLocationInputBoundary {
    private final SelectLocationOutputBoundary output;
    private Stage stage = Stage.INACTIVE;
    private SelectedLocation location;

    /**
     * Connects the selection workflow to its output boundary.
     * @param output recipient of selection results
     */
    public SelectLocationInteractor(SelectLocationOutputBoundary output) {
        this.output = output;
    }

    /** Starts a new draft, discarding any earlier selection. */
    @Override
    public void start() {
        stage = Stage.SELECTING;
        location = null;
        output.present(new SelectLocationOutputData(stage, location, ""));
    }

    /**
     * Validates and replaces the location while selection is active.
     * @param latitude latitude between -90 and 90
     * @param longitude longitude between -180 and 180
     */
    @Override
    public void select(double latitude, double longitude) {
        if (stage != Stage.SELECTING) {
            return;
        }
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            output.present(new SelectLocationOutputData(stage, location, "Choose a valid map location."));
            return;
        }
        location = new SelectedLocation(latitude, longitude);
        output.present(new SelectLocationOutputData(stage, location, ""));
    }

    /** Resumes a confirmed selection while keeping the existing location. */
    @Override
    public void resume() {
        if (stage != Stage.CONFIRMED) { return; }
        stage = Stage.SELECTING;
        output.present(new SelectLocationOutputData(stage, location, ""));
    }

    /** Retains the selected location for the remaining creation steps. */
    @Override
    public void confirm() {
        if (stage != Stage.SELECTING) {
            return;
        }
        if (location == null) {
            output.present(new SelectLocationOutputData(stage, null, "Select a location before continuing."));
            return;
        }
        stage = Stage.CONFIRMED;
        output.present(new SelectLocationOutputData(stage, location, ""));
    }

    /** Discards the draft without accessing persistent pin storage. */
    @Override
    public void cancel() {
        stage = Stage.INACTIVE;
        location = null;
        output.present(new SelectLocationOutputData(stage, null, ""));
    }
}
