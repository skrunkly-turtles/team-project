package view;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import interface_adapter.map.MapController;
import interface_adapter.map.MapPresenter;
import interface_adapter.map.MapViewModel;
import use_case.display_map.DisplayMapInteractor;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises home-screen composition and map navigation on Swing's thread. */
class MapPanelTest {
    /** Missing configuration displays instructions instead of attempting tile downloads. */
    @Test
    void missingTokenShowsSetup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapPanel panel = new MapPanel(null, new MapViewModel());
            assertEquals(1, panel.getComponentCount());
            assertTrue(((JLabel) panel.getComponent(0)).getText().contains("MAPBOX_ACCESS_TOKEN"));
            HomeView home = new HomeView(panel, new MapController(() -> { }));
            assertSame(panel, ((BorderLayout) home.getLayout()).getLayoutComponent(BorderLayout.CENTER));
        });
    }

    /** Zoom buttons respect limits, while drag gestures move the map center. */
    @Test
    void navigationWorksAndZoomIsBounded() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapViewModel viewModel = new MapViewModel();
            MapPanel panel = new MapPanel(
                    new DefaultTileFactory(new MapboxTileFactoryInfo("pk.test")), viewModel);
            MapController controller = new MapController(
                    new DisplayMapInteractor(new MapPresenter(viewModel)));
            new HomeView(panel, controller);
            BorderLayout layout = (BorderLayout) panel.getLayout();
            JXMapViewer viewer = (JXMapViewer) layout.getLayoutComponent(BorderLayout.CENTER);
            JPanel controls = (JPanel) viewer.getComponent(0);
            JButton zoomIn = (JButton) controls.getComponent(0);
            JButton zoomOut = (JButton) controls.getComponent(1);
            assertEquals(43.6629, viewer.getAddressLocation().getLatitude(), 0.00001);
            assertEquals(-79.3957, viewer.getAddressLocation().getLongitude(), 0.00001);
            int initialZoom = viewer.getZoom();
            assertEquals(8, initialZoom);
            zoomIn.doClick(0);
            assertEquals(initialZoom - 1, viewer.getZoom());
            zoomOut.doClick(0);
            assertEquals(initialZoom, viewer.getZoom());
            viewer.setZoom(0);
            zoomIn.doClick(0);
            assertEquals(0, viewer.getZoom());
            viewer.setZoom(19);
            zoomOut.doClick(0);
            assertEquals(19, viewer.getZoom());
            viewer.setZoom(initialZoom);
            viewer.setSize(800, 600);
            viewer.doLayout();
            assertEquals(788, controls.getX() + controls.getWidth());
            assertEquals(588, controls.getY() + controls.getHeight());
            viewer.setSize(1000, 700);
            viewer.doLayout();
            assertEquals(988, controls.getX() + controls.getWidth());
            assertEquals(688, controls.getY() + controls.getHeight());
            Point before = new Point((int) viewer.getCenter().getX(), (int) viewer.getCenter().getY());
            viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_PRESSED, 0,
                    InputEvent.BUTTON1_DOWN_MASK, 300, 300, 1, false, MouseEvent.BUTTON1));
            viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_DRAGGED, 1,
                    InputEvent.BUTTON1_DOWN_MASK, 350, 300, 0, false, MouseEvent.NOBUTTON));
            assertEquals(before.x - 50, viewer.getCenter().getX(), 1.0);
            viewer.dispatchEvent(new MouseWheelEvent(viewer, MouseEvent.MOUSE_WHEEL, 2,
                    0, 400, 300, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, -1));
            assertEquals(initialZoom - 1, viewer.getZoom());
        });
    }
}
