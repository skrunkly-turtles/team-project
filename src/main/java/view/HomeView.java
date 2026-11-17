package view;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import interface_adapter.map.MapController;

/** Arranges the home screen's visual components. */
public class HomeView extends JPanel {
    /**
     * Creates the home screen with an interactive map.
     * @param mapPanel configured map component
     * @param controller input adapter for opening the home map
     */
    public HomeView(MapPanel mapPanel, MapController controller) {
        this(mapPanel, controller, null);
    }

    /**
     * Creates the home screen with its shared navigation sidebar.
     * @param mapPanel configured map component
     * @param controller input adapter for opening the home map
     * @param sidebar navigation component, or null for a map-only screen
     */
    public HomeView(MapPanel mapPanel, MapController controller, HomeSidebar sidebar) {
        super(new BorderLayout());
        if (sidebar != null) {
            add(sidebar, BorderLayout.WEST);
        }
        add(mapPanel, BorderLayout.CENTER);
        controller.displayMap();
    }

}
