package use_case.select_location;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.select_location.SelectLocationOutputData.Stage;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the temporary draft independently of storage or Swing. */
class SelectLocationInteractorTest {
    /** A new flow is empty and cannot continue without a location. */
    @Test
    void requiresSelection() {
        List<SelectLocationOutputData> results = new ArrayList<>();
        SelectLocationInteractor input = new SelectLocationInteractor(results::add);
        input.start();
        assertEquals(Stage.SELECTING, results.get(0).getStage());
        assertNull(results.get(0).getLocation());
        input.confirm();
        assertFalse(results.get(1).getError().isEmpty());
        assertEquals(Stage.SELECTING, results.get(1).getStage());
    }

    /** A replacement is retained on confirmation and cannot be overwritten afterward. */
    @Test
    void replacesAndConfirmsLocation() {
        List<SelectLocationOutputData> results = new ArrayList<>();
        SelectLocationInputBoundary input = new SelectLocationInteractor(results::add);
        input.start();
        input.select(10, 20);
        input.select(43.6, -79.4);
        SelectedLocation chosen = results.get(2).getLocation();
        assertEquals(43.6, chosen.getLatitude());
        assertEquals(-79.4, chosen.getLongitude());
        input.confirm();
        assertSame(chosen, results.get(3).getLocation());
        assertEquals(Stage.CONFIRMED, results.get(3).getStage());
        input.select(0, 0);
        input.confirm();
        assertEquals(4, results.size());
    }

    /** Invalid geographic values are rejected while a previous valid draft is retained. */
    @Test
    void rejectsInvalidCoordinates() {
        List<SelectLocationOutputData> results = new ArrayList<>();
        SelectLocationInteractor input = new SelectLocationInteractor(results::add);
        input.start();
        input.select(0, 0);
        SelectedLocation valid = results.get(1).getLocation();
        double[][] invalid = {{91, 0}, {-91, 0}, {0, 181}, {0, -181},
                {Double.NaN, 0}, {0, Double.NaN}, {Double.POSITIVE_INFINITY, 0},
                {0, Double.NEGATIVE_INFINITY}};
        for (double[] coordinates : invalid) {
            input.select(coordinates[0], coordinates[1]);
            SelectLocationOutputData result = results.get(results.size() - 1);
            assertFalse(result.getError().isEmpty());
            assertSame(valid, result.getLocation());
        }
        input.select(-90, -180);
        assertEquals("", results.get(results.size() - 1).getError());
        input.select(90, 180);
        assertEquals("", results.get(results.size() - 1).getError());
    }

    /** Cancel clears both a selected and a confirmed draft; restarting begins empty. */
    @Test
    void cancelAndRestartDiscardDraft() {
        List<SelectLocationOutputData> results = new ArrayList<>();
        SelectLocationInteractor input = new SelectLocationInteractor(results::add);
        input.select(0, 0);
        input.confirm();
        assertTrue(results.isEmpty());
        input.start();
        input.select(10, 20);
        input.confirm();
        input.cancel();
        assertEquals(Stage.INACTIVE, results.get(3).getStage());
        assertNull(results.get(3).getLocation());
        input.start();
        assertNull(results.get(4).getLocation());
        input.select(1, 2);
        input.start();
        assertNull(results.get(6).getLocation());
        input.cancel();
        assertNull(results.get(7).getLocation());
    }
}
