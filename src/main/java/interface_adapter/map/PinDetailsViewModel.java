package interface_adapter.map;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import use_case.pin_details.PinDetailsOutputData;

/** Observable draft state for the pin-details form. */
public class PinDetailsViewModel {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private PinDetailsOutputData state = new PinDetailsOutputData(null, PinDetailsOutputData.Stage.CANCELLED);
    /**
     * Registers an observer.
     * @param listener draft observer
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) { changes.addPropertyChangeListener(listener); }
    /** @return latest details snapshot */
    public PinDetailsOutputData getState() { return state; }
    /**
     * Replaces the state and notifies the form.
     * @param result new presentation state
     */
    public void setState(PinDetailsOutputData result) {
        state = result;
        changes.firePropertyChange("details", null, result);
    }
}
