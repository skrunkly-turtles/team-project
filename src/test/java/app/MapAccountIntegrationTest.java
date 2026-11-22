package app;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import org.jxmapviewer.viewer.WaypointPainter;
import use_case.select_location.SelectedLocation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.jxmapviewer.JXMapViewer;
import view.HomeView;
import view.LoginView;
import view.MapPanel;
import view.ProfileView;
import view.SignupView;
import view.ViewManager;

/** Tests the assembled account UI and map without displaying a window. */
class MapAccountIntegrationTest {
  @TempDir Path directory;

  /** Signup/login reaches the map; profile returns to it; logout clears the home identity. */
  @Test
  void accountJourneyUsesTheRealMapHome() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      List<SelectedLocation> handoffs = new ArrayList<>();
      int[] filterRequests = {0};
      ViewManager screens = new AppBuilder().accountFile(directory.resolve("users.json"))
          .mapboxToken("pk.test").onLocationSelected(handoffs::add)
          .onFilterPins(() -> filterRequests[0]++).buildContent();
      assertInstanceOf(LoginView.class, visibleScreen(screens));
      click(visibleScreen(screens), "Create account");
      assertInstanceOf(SignupView.class, visibleScreen(screens));
      Container signup = visibleScreen(screens);
      fields(signup, JTextField.class).get(0).setText("maptester");
      List<JPasswordField> signupPasswords = fields(signup, JPasswordField.class);
      signupPasswords.get(0).setText("testpassword123");
      signupPasswords.get(1).setText("testpassword123");
      click(signup, "Create account");
      assertInstanceOf(LoginView.class, visibleScreen(screens));
      Container login = visibleScreen(screens);
      fields(login, JTextField.class).get(0).setText("maptester");
      fields(login, JPasswordField.class).get(0).setText("testpassword123");
      click(login, "Log in");
      Container home = visibleScreen(screens);
      assertInstanceOf(HomeView.class, home);
      assertTrue(fields(home, JLabel.class).stream().anyMatch(label -> "Welcome, maptester".equals(label.getText())));
      assertEquals(1, fields(home, MapPanel.class).size());
      JXMapViewer viewer = fields(home, JXMapViewer.class).get(0);
      assertEquals(8, viewer.getZoom());
      assertEquals(43.6629, viewer.getAddressLocation().getLatitude(), 0.00001);
            click(home, "Filter Pins");
      assertEquals(1, filterRequests[0]);
      click(home, "Add Pin");
      viewer.setSize(800, 600);
      viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_PRESSED, 0,
          InputEvent.BUTTON1_DOWN_MASK, 400, 300, 1, false, MouseEvent.BUTTON1));
      viewer.dispatchEvent(new MouseEvent(viewer, MouseEvent.MOUSE_RELEASED, 1,
          0, 400, 300, 1, false, MouseEvent.BUTTON1));
      click(home, "Continue");
      assertEquals(1, handoffs.size());
      assertEquals(43.6629, handoffs.get(0).getLatitude(), 0.001);
      assertEquals(1, ((WaypointPainter<?>) viewer.getOverlayPainter()).getWaypoints().size());
      click(home, "Profile");
      assertInstanceOf(ProfileView.class, visibleScreen(screens));
      click(visibleScreen(screens), "Back to map");
      assertSame(home, visibleScreen(screens));
      click(home, "Log Out");
      assertInstanceOf(LoginView.class, visibleScreen(screens));
      assertFalse(fields(home, JLabel.class).stream().anyMatch(label -> "Welcome, maptester".equals(label.getText())));
      assertTrue(((WaypointPainter<?>) viewer.getOverlayPainter()).getWaypoints().isEmpty());
      assertTrue(fields(home, JButton.class).stream()
          .anyMatch(button -> "Add Pin".equals(button.getText()) && button.isEnabled()));
    });
  }

  /** Missing map configuration leaves the account screens runnable and supplies setup instructions. */
  @Test
  void missingTokenDoesNotBreakAccountAssembly() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      ViewManager screens = new AppBuilder().accountFile(directory.resolve("empty.json"))
          .mapboxToken(null).buildContent();
      assertInstanceOf(LoginView.class, visibleScreen(screens));
      assertTrue(fields(screens, JXMapViewer.class).isEmpty());
      assertTrue(fields(screens, JLabel.class).stream()
          .anyMatch(label -> label.getText() != null && label.getText().contains("MAPBOX_ACCESS_TOKEN")));
    });
  }

  /**
   * Finds the card currently selected by navigation.
   * @param screens navigation container
   * @return visible card
   */
  private static Container visibleScreen(ViewManager screens) {
    for (Component child : screens.getComponents()) {
      if (child.isVisible()) { return (Container) child; }
    }
    throw new AssertionError("No visible screen");
  }

  /**
   * Clicks a button inside one screen.
   * @param root screen to inspect
   * @param text button label
   */
  private static void click(Container root, String text) {
    JButton button = fields(root, JButton.class).stream()
        .filter(candidate -> text.equals(candidate.getText())).findFirst().orElseThrow();
    button.doClick(0);
  }

  /**
   * Collects controls in component order.
   * @param root container to inspect
   * @param type control class
   * @param <T> requested component type
   * @return matching controls
   */
  private static <T> List<T> fields(Container root, Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Component child : root.getComponents()) {
      if (type.isInstance(child)) { result.add(type.cast(child)); }
      if (child instanceof Container) { result.addAll(fields((Container) child, type)); }
    }
    return result;
  }
}
