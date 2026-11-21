package use_case.login;

/** Receives the result of a login attempt. */
public interface LoginOutputBoundary {
    /** Displays successful authentication. */
    void prepareSuccessView(LoginOutputData outputData);

    /** Displays authentication failure. */
    void prepareFailView(String errorMessage);
}
