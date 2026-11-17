package use_case.select_location;

/** Requests for temporary location selection. */
public interface SelectLocationInputBoundary {
    /** Starts a fresh selection without saving a pin. */
    void start();

    /**
     * Replaces the temporary selection.
     * @param latitude latitude in degrees
     * @param longitude longitude in degrees
     */
    void select(double latitude, double longitude);

    /** Resumes an existing confirmed selection for revision. */
    void resume();

    /** Continues only when a valid location has been selected. */
    void confirm();

    /** Discards the draft and exits selection. */
    void cancel();
}
