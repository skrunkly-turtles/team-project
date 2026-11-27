package interface_adapter.read_pins;

import java.util.ArrayList;
import java.util.List;
import models.Pin.PinData;

/** Immutable display state for the pins list. */
public final class ReadPinsState {
    private final List<PinData> pins;
    private final String message;

    /** Creates an empty state. */
    public ReadPinsState() {
        this(new ArrayList<>(), "");
    }

    /** Creates a state with the given pins and message. */
    public ReadPinsState(List<PinData> pins, String message) {
        this.pins = new ArrayList<>(pins);
        this.message = message;
    }

    /** Returns a copy of the pins to display. */
    public List<PinData> getPins() {
        return new ArrayList<>(pins);
    }

    /** Returns a status message, or an empty string. */
    public String getMessage() {
        return message;
    }
}