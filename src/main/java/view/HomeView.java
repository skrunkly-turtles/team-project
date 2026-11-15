package view;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/** Arranges the home screen's visual components. */
public class HomeView extends JPanel {
    /**
     * Creates the home screen with an interactive map.
     * @param mapPanel configured map component
     */
    public HomeView(MapPanel mapPanel) {
        super(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
    }
}
