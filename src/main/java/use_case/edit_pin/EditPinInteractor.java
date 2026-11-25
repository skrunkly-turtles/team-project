package use_case.edit_pin;

import models.Book;
import models.Coordinate;
import models.Passage;
import models.Pin;
import models.User;
import use_case.session.CurrentUserSession;

/** Applies validated edits to a pin owned by the signed-in user. */
public final class EditPinInteractor implements EditPinInputBoundary {
    private final EditPinUserDataAccessInterface accounts;
    private final EditPinOutputBoundary presenter;
    private final CurrentUserSession session;
    private final int maxPassageWords;

    /** Creates the interactor with only inward-facing interfaces and the passage limit. */
    public EditPinInteractor(EditPinUserDataAccessInterface accounts,
                             EditPinOutputBoundary presenter, CurrentUserSession session, int maxPassageWords) {
        this.accounts = accounts;
        this.presenter = presenter;
        this.session = session;
        this.maxPassageWords = maxPassageWords;
    }

    /** Validates the request and replaces the pin with its edited version. */
    @Override
    public void execute(EditPinInputData input) {
        String username = session.getCurrentUsername();
        if (username == null) {
            presenter.prepareFailureView("Please sign in to edit a pin.");
            return;
        }
        User current = accounts.get(username);
        if (current == null) {
            presenter.prepareFailureView("Your account could not be found.");
            return;
        }
        Pin existing = null;
        for (Pin pin : current.getPins()) {
            if (pin.getId() == input.getPinId()) {
                existing = pin;
            }
        }
        if (existing == null) {
            presenter.prepareFailureView("You can only edit pins you created.");
            return;
        }
        String text = trimmed(input.getPassageText());
        if (text.isEmpty()) {
            presenter.prepareFailureView("Passage is required.");
            return;
        }
        int wordCount = text.split("\\s+").length;
        if (wordCount > maxPassageWords) {
            presenter.prepareFailureView("Passage must be at most " + maxPassageWords + " words.");
            return;
        }
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(trimmed(input.getLatitude()));
            longitude = Double.parseDouble(trimmed(input.getLongitude()));
        } catch (NumberFormatException exception) {
            presenter.prepareFailureView("Latitude and longitude must be numbers.");
            return;
        }
        if (Double.isNaN(latitude) || latitude < -90 || latitude > 90) {
            presenter.prepareFailureView("Latitude must be between -90 and 90.");
            return;
        }
        if (Double.isNaN(longitude) || longitude < -180 || longitude > 180) {
            presenter.prepareFailureView("Longitude must be between -180 and 180.");
            return;
        }
        int year = 0;
        String yearText = trimmed(input.getYear());
        if (!yearText.isEmpty()) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException exception) {
                presenter.prepareFailureView("Year must be a whole number.");
                return;
            }
        }
        Pin updated = new Pin(existing.getId(), username,
                new Coordinate(longitude, latitude),
                new Passage(new Book(trimmed(input.getBookTitle()), trimmed(input.getAuthor()), year),
                        text, wordCount),
                trimmed(input.getDescription()), existing.isVisible());
        try {
            accounts.updatePin(username, updated);
            presenter.prepareSuccessView(new EditPinOutputData(username, updated.getId()),
                    "Pin updated.");
        } catch (IllegalStateException exception) {
            presenter.prepareFailureView("Could not save changes. Please try again.");
        }
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}