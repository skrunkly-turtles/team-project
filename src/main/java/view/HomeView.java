package view;

import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.profile.ProfileController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Temporary authenticated home screen until John's interactive map is integrated.
 *
 * <p>This view contains no friend or pin management implementation.
 */
public final class HomeView extends JPanel {
  private final JLabel heading = new JLabel("Welcome", SwingConstants.CENTER);

  /** Constructs navigation from the authenticated home screen to the profile. */
  public HomeView(LogoutController logoutController, ProfileController profileController,
      HomeViewModel homeViewModel) {
    setLayout(new BorderLayout(16, 16));
    setBorder(BorderFactory.createEmptyBorder(32, 35, 32, 35));
    heading.setFont(heading.getFont().deriveFont(22f));
    add(heading, BorderLayout.NORTH);
    JLabel placeholder = new JLabel(
        "John's interactive map will replace this home-screen placeholder.",
        SwingConstants.CENTER);
    add(placeholder, BorderLayout.CENTER);
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
      heading.setText(state.getUsername() == null ? "Welcome"
          : "Welcome, " + state.getUsername());
    });
  }
}
