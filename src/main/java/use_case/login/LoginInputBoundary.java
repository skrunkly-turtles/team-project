package use_case.login;

/** Input boundary for authenticating a registered user. */
public interface LoginInputBoundary {
    /** Attempts to authenticate the submitted credentials. */
    void execute(LoginInputData inputData);
}
