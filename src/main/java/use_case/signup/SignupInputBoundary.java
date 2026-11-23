package use_case.signup;

/** Input boundary for creating a new user account. */
public interface SignupInputBoundary {
    /** Runs the signup use case. */
    void execute(SignupInputData inputData);
}
