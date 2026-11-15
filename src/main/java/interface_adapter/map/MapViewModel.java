package interface_adapter.map;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Observable display state; contains no Swing or map-provider classes. */
public class MapViewModel {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private double latitude;
    private double longitude;
    private int zoom;

    /**
     * Registers a view to receive viewport updates.
     * @param listener observer of the viewport property
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    /**
     * Replaces the display state and notifies the observing view.
     * @param newLatitude latitude in degrees
     * @param newLongitude longitude in degrees
     * @param newZoom conventional zoom level, increasing with detail
     */
    public void setViewport(double newLatitude, double newLongitude, int newZoom) {
        latitude = newLatitude;
        longitude = newLongitude;
        zoom = newZoom;
        changes.firePropertyChange("viewport", null, null);
    }

    /** @return latitude to display */
    public double getLatitude() { return latitude; }

    /** @return longitude to display */
    public double getLongitude() { return longitude; }

    /** @return conventional zoom level to display */
    public int getZoom() { return zoom; }
}
