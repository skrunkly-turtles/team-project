package interface_adapter.map;

import use_case.display_map.DisplayMapInputBoundary;

/** Routes home-map requests through the use-case input boundary. */
public class MapController {
    private final DisplayMapInputBoundary displayMap;

    /**
     * Creates the controller for home-map requests.
     * @param displayMap home-map input boundary
     */
    public MapController(DisplayMapInputBoundary displayMap) {
        this.displayMap = displayMap;
    }

    /** Requests the initial map presentation. */
    public void displayMap() {
        displayMap.execute();
    }
}
