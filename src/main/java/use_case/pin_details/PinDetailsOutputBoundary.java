package use_case.pin_details;

/** Presents draft results independently of Swing. */
public interface PinDetailsOutputBoundary {
    /**
     * Publishes the current draft and navigation stage.
     * @param result details result
     */
    void present(PinDetailsOutputData result);
}
