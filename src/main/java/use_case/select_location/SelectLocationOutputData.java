package use_case.select_location;

/** Snapshot of temporary selection state. */
public final class SelectLocationOutputData {
    /** Stages of selecting a location before book selection. */
    public enum Stage { INACTIVE, SELECTING, CONFIRMED }
    private final Stage stage;
    private final SelectedLocation location;
    private final String error;

    /**
     * Creates a result for presentation.
     * @param stage current creation stage
     * @param location selected location, or null
     * @param error validation message, empty on success
     */
    public SelectLocationOutputData(Stage stage, SelectedLocation location, String error) {
        this.stage = stage;
        this.location = location;
        this.error = error;
    }

    /** @return current stage */
    public Stage getStage() { return stage; }

    /** @return immutable selected location, or null */
    public SelectedLocation getLocation() { return location; }

    /** @return validation message */
    public String getError() { return error; }
}
