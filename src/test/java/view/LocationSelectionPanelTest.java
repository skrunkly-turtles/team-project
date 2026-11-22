package view;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.WaypointPainter;
import interface_adapter.map.*;
import use_case.select_location.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tests map gestures through the controller, interactor, presenter, and view. */
class LocationSelectionPanelTest {
    /** Selection moves, survives confirmation, and disappears after cancel. */
    @Test
    void selectionFlowAndHandoff() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapViewModel mapState = new MapViewModel();
            MapPanel map = new MapPanel(new DefaultTileFactory(new MapboxTileFactoryInfo("pk.test")), mapState);
            List<SelectedLocation> handoffs = new ArrayList<>();
            LocationSelectionViewModel selection = new LocationSelectionViewModel();
            LocationSelectionController controller = new LocationSelectionController(new SelectLocationInteractor(
                    new LocationSelectionPresenter(selection, handoffs::add)));
            map.enableLocationSelection(controller, selection);
            mapState.setViewport(43.6629, -79.3957, 11);
            JXMapViewer viewer = (JXMapViewer) ((BorderLayout) map.getLayout())
                    .getLayoutComponent(BorderLayout.CENTER);
            viewer.setSize(800, 600);
            LocationSelectionPanel toolbar = (LocationSelectionPanel) ((BorderLayout) map.getLayout())
                    .getLayoutComponent(BorderLayout.NORTH);
            JButton confirm = (JButton) toolbar.getComponent(0);
            JButton cancel = (JButton) toolbar.getComponent(1);
            assertFalse(confirm.isEnabled());
            assertFalse(toolbar.isVisible());
            controller.start();
            assertTrue(toolbar.isVisible());
            gesture(viewer, 400, 300, 400, 300);
            assertTrue(confirm.isEnabled());
            SelectedLocation first = selection.getState().getLocation();
            assertEquals(43.6629, first.getLatitude(), 0.001);
            assertEquals(-79.3957, first.getLongitude(), 0.001);
            gesture(viewer, 450, 300, 450, 300);
            SelectedLocation moved = selection.getState().getLocation();
            assertNotEquals(first.getLongitude(), moved.getLongitude());
            gesture(viewer, 450, 300, 500, 300);
            assertSame(moved, selection.getState().getLocation());
            confirm.doClick(0);
            assertEquals(1, handoffs.size());
            assertSame(moved, handoffs.get(0));
            assertSame(moved, selection.getState().getLocation());
            assertFalse(confirm.isEnabled());
            assertEquals(1, ((WaypointPainter<?>) viewer.getOverlayPainter()).getWaypoints().size());
            cancel.doClick(0);
            assertNull(selection.getState().getLocation());
            assertTrue(((WaypointPainter<?>) viewer.getOverlayPainter()).getWaypoints().isEmpty());
            assertFalse(toolbar.isVisible());
            controller.start();
            assertFalse(confirm.isEnabled());
            cancel.doClick(0);
            assertEquals(1, handoffs.size());
        });
    }

    /**
     * Dispatches a press, optional drag, and release without painting network tiles.
     * @param viewer map receiving the gesture
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param endX final horizontal position
     * @param endY final vertical position
     */
    private static void gesture(JXMapViewer viewer, int x, int y, int endX, int endY) {
        viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_PRESSED, 0,
                InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
        if (x != endX || y != endY) {
            viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_DRAGGED, 1,
                    InputEvent.BUTTON1_DOWN_MASK, endX, endY, 0, false, MouseEvent.NOBUTTON));
        }
        viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_RELEASED, 2,
                0, endX, endY, 1, false, MouseEvent.BUTTON1));
    }
}
