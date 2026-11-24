package interface_adapter.map;

import use_case.select_location.SelectLocationInputBoundary;

/** Routes map-selection actions to the input boundary. */
public class LocationSelectionController {
    private final SelectLocationInputBoundary input;

    /**
     * Connects the controller to the selection use case.
     * @param input selection input boundary
     */
    public LocationSelectionController(SelectLocationInputBoundary input) { this.input = input; }

    /** Starts location selection. */
    public void start() { input.start(); }

    /**
     * Passes geographic coordinates from the map adapter.
     * @param latitude latitude in degrees
     * @param longitude longitude in degrees
     */
    public void select(double latitude, double longitude) { input.select(latitude, longitude); }

    /** Returns to the existing selection without clearing the marker. */
    public void resume() { input.resume(); }

    /** Requests continuation to book selection. */
    public void confirm() { input.confirm(); }

    /** Discards location selection. */
    public void cancel() { input.cancel(); }
}
