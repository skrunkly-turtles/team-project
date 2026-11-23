package use_case.display_map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests the use case without Swing, Mapbox, a network, or a real presenter. */
class DisplayMapInteractorTest {
    /** Ensures the interactor publishes a geographic viewport through its boundary. */
    @Test
    void publishesInitialViewport() {
        DisplayMapOutputData[] result = new DisplayMapOutputData[1];
        DisplayMapInteractor interactor = new DisplayMapInteractor(output -> result[0] = output);
        interactor.execute();
        assertNotNull(result[0]);
        assertEquals(43.6629, result[0].getLatitude());
        assertEquals(-79.3957, result[0].getLongitude());
        assertEquals(11, result[0].getZoom());
    }
}
