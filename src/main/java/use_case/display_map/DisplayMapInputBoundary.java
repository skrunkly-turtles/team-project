package use_case.display_map;

/** Defines the request to open the home map. */
public interface DisplayMapInputBoundary {
    /** Prepares the initial home-map viewport and presents the result. */
    void execute();
}
