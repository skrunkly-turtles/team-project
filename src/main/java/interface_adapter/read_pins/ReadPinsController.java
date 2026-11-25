package interface_adapter.read_pins;

import use_case.read_pins.ReadPinsInputBoundary;

/** Requests a refresh of the pins the signed-in user may see. */
public final class ReadPinsController {
    private final ReadPinsInputBoundary interactor;

    /** Creates the controller. */
    public ReadPinsController(ReadPinsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Loads the accessible pins. */
    public void execute() {
        interactor.execute();
    }
}