package use_case.delete_pin;

import models.Pin;
import models.User;
import use_case.session.CurrentUserSession;

public final class DeletePinInteractor implements DeletePinInputBoundary {
    private final DeletePinUserDataAccessInterface accounts;
    private final DeletePinOutputBoundary presenter;
    private final CurrentUserSession session;

    public DeletePinInteractor(DeletePinUserDataAccessInterface accounts,
                               DeletePinOutputBoundary presenter, CurrentUserSession session) {
        this.accounts = accounts;
        this.presenter = presenter;
        this.session = session;
    }

    /** Checks ownership, then removes the pin from the user's account. */
    @Override
    public void execute(DeletePinInputData input) {
        String username = session.getCurrentUsername();
        if (username == null) {
            presenter.prepareFailureView("Please sign in to delete a pin.");
            return;
        }
        User current = accounts.get(username);
        if (current == null) {
            presenter.prepareFailureView("Your account could not be found.");
            return;
        }
        boolean owned = false;
        for (Pin pin : current.getPins()) {
            if (pin.getId() == input.getPinId()) {
                owned = true;
            }
        }
        if (!owned) {
            presenter.prepareFailureView("You can only delete pins you created.");
            return;
        }
        try {
            accounts.deletePin(username, input.getPinId());
            presenter.prepareSuccessView(new DeletePinOutputData(username, input.getPinId()),
                    "Pin deleted.");
        } catch (IllegalStateException exception) {
            presenter.prepareFailureView("Could not delete pin. Please try again.");
        }
    }
}