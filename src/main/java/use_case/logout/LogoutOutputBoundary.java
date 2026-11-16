package use_case.logout;

/** Receives completion of the logout operation. */
public interface LogoutOutputBoundary {
    /** Returns the application to its unauthenticated UI state. */
    void prepareSuccessView();
}
