package use_case.signup;

/** Receives successful or unsuccessful signup results. */
public interface SignupOutputBoundary {
    /** Displays a success result without exposing credentials. */
    void prepareSuccessView(SignupOutputData outputData);

    /** Displays a validation or storage error. */
    void prepareFailView(String errorMessage);
}
