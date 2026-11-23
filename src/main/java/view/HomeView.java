package view;

import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.map.MapController;
import interface_adapter.profile.ProfileController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Displays the map and authenticated account navigation. */
public class HomeView extends JPanel {
  /**
   * Creates the reusable map screen.
   * @param mapPanel configured map component
   * @param controller input adapter for map initialization
   */
  public HomeView(MapPanel mapPanel, MapController controller) {
    super(new BorderLayout(8, 8));
    add(mapPanel, BorderLayout.CENTER);
    controller.displayMap();
  }

  /**
   * Adds account actions to the shared map screen.
   * @param mapPanel configured map component
   * @param controller map initialization controller
   * @param logoutController authenticated logout action
   * @param profileController profile navigation action
   * @param homeViewModel authenticated user's display state
   */
  public HomeView(MapPanel mapPanel, MapController controller,
      LogoutController logoutController, ProfileController profileController,
      HomeViewModel homeViewModel) {
    this(mapPanel, controller);
    JLabel heading = new JLabel("Welcome", SwingConstants.CENTER);
    heading.setFont(heading.getFont().deriveFont(22f));
    add(heading, BorderLayout.NORTH);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    JButton profile = new JButton("Open My Profile");
    JButton logout = new JButton("Log Out");
    profile.addActionListener(event -> profileController.execute());
    logout.addActionListener(event -> logoutController.execute());
    actions.add(profile);
    actions.add(logout);
    add(actions, BorderLayout.SOUTH);
    homeViewModel.addPropertyChangeListener(event -> {
      HomeState state = (HomeState) event.getNewValue();
      heading.setText(state.getUsername() == null ? "Welcome" : "Welcome, " + state.getUsername());
    });
  }
}
