package views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Observable UI state holder; Swing views observe its state via PropertyChangeListener. */
public class ViewModel<T> {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private T state;

    /** Creates a view model with initial state. */
    public ViewModel(T initialState) { this.state = initialState; }

    /** Returns the current UI state. */
    public T getState() { return state; }

    /** Changes state and notifies subscribed Swing views. */
    public void setState(T nextState) {
        T previous = state;
        state = nextState;
        support.firePropertyChange("state", previous, nextState);
    }

    /** Registers one observer to be notified when state changes. */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
