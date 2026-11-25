package use_case.read_pins;

/** Receives the outcome of the read-pins use case. */
public interface ReadPinsOutputBoundary {
    /** Presents the accessible pins. */
    void prepareSuccessView(ReadPinsOutputData output);

    /** Presents a failure to load pins. */
    void prepareFailureView(String message);
}