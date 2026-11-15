package use_case.display_map;

/** Receives the home-map result independently of its presentation technology. */
public interface DisplayMapOutputBoundary {
    /**
     * Presents the initial viewport.
     * @param outputData geographic center and conventional zoom level
     */
    void present(DisplayMapOutputData outputData);
}
