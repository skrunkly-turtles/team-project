package interface_adapter.logout;

import use_case.logout.LogoutInputBoundary;

/** Invokes the session logout use case. */
public final class LogoutController {
    private final LogoutInputBoundary interactor;

    /** Injects the logout input boundary. */
    public LogoutController(LogoutInputBoundary interactor) { this.interactor = interactor; }

    /** Logs out the currently active user. */
    public void execute() { interactor.execute(); }
}
