package interface_adapter.map;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import use_case.select_location.SelectLocationOutputData;
import use_case.select_location.SelectLocationOutputData.Stage;

/** Observable state for location controls and the temporary marker. */
public class LocationSelectionViewModel {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private SelectLocationOutputData state = new SelectLocationOutputData(Stage.INACTIVE, null, "");

    /**
     * Registers an observer of selection updates.
     * @param listener observer to notify
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    /** @return current selection snapshot */
    public SelectLocationOutputData getState() { return state; }

    /**
     * Replaces the snapshot and notifies the view.
     * @param result selection result
     */
    public void setState(SelectLocationOutputData result) {
        state = result;
        changes.firePropertyChange("selection", null, result);
    }
}
