package view;

import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.map.MapController;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Displays the map and shared sidebar. */
public class HomeView extends JPanel {
  /**
   * Creates the reusable map screen.
   * @param mapPanel configured map component
   * @param controller map initialization adapter
   */
  public HomeView(MapPanel mapPanel, MapController controller) {
    this(mapPanel, controller, null);
  }

  /**
   * Adds the sidebar beside the map.
   * @param mapPanel configured map component
   * @param controller map initialization adapter
   * @param sidebar shared navigation, or null for a map-only screen
   */
  public HomeView(MapPanel mapPanel, MapController controller, HomeSidebar sidebar) {
    super(new BorderLayout(8, 8));
    if (sidebar != null) { add(sidebar, BorderLayout.WEST); }
    add(mapPanel, BorderLayout.CENTER);
    controller.displayMap();
  }

  /**
   * Displays the logged-in user's greeting above the map and sidebar.
   * @param mapPanel configured map component
   * @param controller map initialization adapter
   * @param sidebar sidebar with feature callbacks
   * @param homeViewModel authenticated display state
   */
  public HomeView(MapPanel mapPanel, MapController controller, HomeSidebar sidebar,
      HomeViewModel homeViewModel) {
    this(mapPanel, controller, sidebar);
    JLabel heading = new JLabel("Welcome", SwingConstants.CENTER);
    heading.setFont(heading.getFont().deriveFont(22f));
    add(heading, BorderLayout.NORTH);
    homeViewModel.addPropertyChangeListener(event -> {
      HomeState state = (HomeState) event.getNewValue();
      heading.setText(state.getUsername() == null ? "Welcome" : "Welcome, " + state.getUsername());
    });
  }
}
