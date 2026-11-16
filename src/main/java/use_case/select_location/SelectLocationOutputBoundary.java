package use_case.select_location;

/** Presents location-selection results without UI dependencies. */
public interface SelectLocationOutputBoundary {
    /**
     * Updates selection state and handles confirmed-location navigation.
     * @param result latest selection snapshot
     */
    void present(SelectLocationOutputData result);
}
