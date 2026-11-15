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
        super(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
        controller.displayMap();
    }
}
