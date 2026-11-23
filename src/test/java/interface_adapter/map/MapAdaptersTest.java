package interface_adapter.map;

import org.junit.jupiter.api.Test;
import use_case.display_map.DisplayMapOutputData;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies adapter contracts and observable state without constructing a UI. */
class MapAdaptersTest {
    /** The controller calls any implementation of the input boundary. */
    @Test
    void controllerDelegatesToInputBoundary() {
        int[] requests = {0};
        new MapController(() -> requests[0]++).displayMap();
        assertEquals(1, requests[0]);
    }

    /** Presenter updates state before notifying the observing view. */
    @Test
    void presenterUpdatesAndNotifiesViewModel() {
        MapViewModel model = new MapViewModel();
        int[] notifications = {0};
        model.addPropertyChangeListener(event -> {
            assertEquals("viewport", event.getPropertyName());
            assertEquals(10.0, model.getLatitude());
            assertEquals(-20.0, model.getLongitude());
            assertEquals(5, model.getZoom());
            notifications[0]++;
        });
        new MapPresenter(model).present(new DisplayMapOutputData(10.0, -20.0, 5));
        assertEquals(1, notifications[0]);
    }
}
