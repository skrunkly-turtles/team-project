package use_case.read_pins;

import models.Pin.PinData;
import java.util.ArrayList;
import java.util.List;

/** The pins the signed-in user is allowed to see. */
public final class ReadPinsOutputData {
    private final List<PinData> pins;

    /** Records a snapshot of the accessible pins. */
    public ReadPinsOutputData(List<PinData> pins) {
        this.pins = new ArrayList<>(pins);
    }

    /** Returns a copy of the accessible pins. */
    public List<PinData> getPins() {
        return new ArrayList<>(pins);
    }
}