package use_case.logout;

import use_case.session.CurrentUserSession;

/** Clears authentication state and notifies the presenter. */
public final class LogoutInteractor implements LogoutInputBoundary {
    private final CurrentUserSession session;
    private final LogoutOutputBoundary presenter;

    /** Connects the session and output boundary. */
    public LogoutInteractor(CurrentUserSession session, LogoutOutputBoundary presenter) {
        this.session = session;
        this.presenter = presenter;
    }

    /** Logs out even if the session was already empty. */
    @Override
    public void execute() {
        session.signOut();
        presenter.prepareSuccessView();
    }
}
