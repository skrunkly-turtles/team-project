package use_case.read_pins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import models.Book;
import models.Coordinate;
import models.Passage;
import models.Pin;
import models.User;
import use_case.session.CurrentUserSession;

/** Selects the pins the signed-in user may see: public, a friend's, or their own. */
public final class ReadPinsInteractor implements ReadPinsInputBoundary {
    private final ReadPinsUserDataAccessInterface accounts;
    private final ReadPinsOutputBoundary presenter;
    private final CurrentUserSession session;

    /** Creates the interactor with only inward-facing interfaces. */
    public ReadPinsInteractor(ReadPinsUserDataAccessInterface accounts,
                              ReadPinsOutputBoundary presenter, CurrentUserSession session) {
        this.accounts = accounts;
        this.presenter = presenter;
        this.session = session;
    }

    /** Applies the access rules to every stored pin and presents the allowed ones. */
    @Override
    public void execute() {
        String username = session.getCurrentUsername();
        if (username == null) {
            presenter.prepareFailureView("Please sign in to view pins.");
            return;
        }
        if (accounts.get(username) == null) {
            presenter.prepareFailureView("Your account could not be found.");
            return;
        }
        List<Pin.PinData> allowed = new ArrayList<>();
        for (User owner : accounts.getAll()) {
            boolean own = owner.getUsername().equals(username);
            boolean friend = owner.getFriends().contains(username);
            for (Pin pin : owner.getPins()) {
                if (own || friend || pin.isVisible()) {
                    allowed.add(toData(owner.getUsername(), pin));
                }
            }
        }
        allowed.sort(Comparator.comparing(Pin.PinData::owner).thenComparingInt(Pin.PinData::id));
        presenter.prepareSuccessView(new ReadPinsOutputData(allowed));
    }

    /** Copies a pin into display data, tolerating pins with missing parts. */
    private static Pin.PinData toData(String owner, Pin pin) {
        Passage passage = pin.getPassage();
        Book book = passage == null ? null : passage.getBook();
        Coordinate coordinate = pin.getCoordinate();
        return new Pin.PinData(pin.getId(), owner, coordinate, passage, pin.getDescription(),
                pin.isVisible());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}