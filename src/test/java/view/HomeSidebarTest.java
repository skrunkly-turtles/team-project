package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import interface_adapter.map.*;
import use_case.select_location.*;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies navigation callbacks and Add Pin integration. */
class HomeSidebarTest {
    /** Filter/profile/logout delegate once; an active draft blocks duplicate creation. */
    @Test
    void routesActionsAndDisablesDuplicateDrafts() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int[] calls = {0, 0, 0, 0};
            HomeSidebar sidebar = new HomeSidebar(() -> calls[0]++, () -> calls[1]++, () -> calls[2]++, () -> calls[3]++);
            List<JButton> buttons = new ArrayList<>();
            for (Component component : sidebar.getComponents()) {
                if (component instanceof JButton) { buttons.add((JButton) component); }
            }
            assertEquals(4, buttons.size());
            assertEquals("Add Pin", buttons.get(0).getText());
            assertEquals("Filter Pins", buttons.get(1).getText());
            assertEquals("Profile", buttons.get(2).getText());
            assertEquals("Log Out", buttons.get(3).getText());
            sidebar.setSize(180, 600);
            sidebar.doLayout();
            assertTrue(buttons.get(2).getY() > 450);
            assertTrue(buttons.get(3).getY() > buttons.get(2).getY());
            for (JButton button : buttons) { button.doClick(0); }
            assertArrayEquals(new int[] {1, 1, 1, 1}, calls);
            sidebar.setCreationActive(true);
            buttons.get(0).doClick(0);
            assertEquals(1, calls[0]);
            sidebar.setCreationActive(false);
            buttons.get(0).doClick(0);
            assertEquals(2, calls[0]);
        });
    }

    /** Home mounts the sidebar and Add Pin starts the existing selection use case. */
    @Test
    void mountsSidebarAndStartsSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LocationSelectionViewModel selection = new LocationSelectionViewModel();
            LocationSelectionController selectionController = new LocationSelectionController(
                    new SelectLocationInteractor(new LocationSelectionPresenter(selection, location -> { })));
            HomeSidebar sidebar = new HomeSidebar(selectionController::start, () -> { }, () -> { }, () -> { });
            selection.addPropertyChangeListener(event -> sidebar.setCreationActive(
                    selection.getState().getStage() != SelectLocationOutputData.Stage.INACTIVE));
            MapPanel map = new MapPanel(null, new MapViewModel());
            int[] initialDisplays = {0};
            HomeView home = new HomeView(map, new MapController(() -> initialDisplays[0]++), sidebar);
            assertSame(sidebar, ((BorderLayout) home.getLayout()).getLayoutComponent(BorderLayout.WEST));
            assertSame(map, ((BorderLayout) home.getLayout()).getLayoutComponent(BorderLayout.CENTER));
            assertEquals(1, initialDisplays[0]);
            JButton addPin = null;
            for (Component component : sidebar.getComponents()) {
                if (component instanceof JButton && ((JButton) component).getText().equals("Add Pin")) {
                    addPin = (JButton) component;
                }
            }
            assertNotNull(addPin);
            addPin.doClick(0);
            assertEquals(SelectLocationOutputData.Stage.SELECTING, selection.getState().getStage());
            assertFalse(addPin.isEnabled());
            selectionController.cancel();
            assertTrue(addPin.isEnabled());
        });
    }
}
