package use_case.display_map;

/** Prepares the application's initial home-map viewport. */
public class DisplayMapInteractor implements DisplayMapInputBoundary {
    private final DisplayMapOutputBoundary outputBoundary;

    /**
     * Injects the output boundary rather than a concrete presenter or view.
     * @param outputBoundary recipient of the initial viewport
     */
    public DisplayMapInteractor(DisplayMapOutputBoundary outputBoundary) {
        this.outputBoundary = outputBoundary;
    }

    /** Opens the home map centered near the University of Toronto. */
    @Override
    public void execute() {
        outputBoundary.present(new DisplayMapOutputData(43.6629, -79.3957, 11));
    }
}
