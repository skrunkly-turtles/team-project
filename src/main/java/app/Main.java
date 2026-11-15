package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Desktop entry point for the CSC207 Passage Map account and profile prototype. */
public final class Main {
  /** Prevents constructing a utility-only entry point. */
  private Main() {
  }

  /**
   * Starts the application on the Swing event-dispatch thread.
   *
   * @param args unused command-line arguments
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception exception) {
        // A missing system look and feel is non-fatal; Swing uses its default.
      }
      new AppBuilder().build().setVisible(true);
    });
  }
}
