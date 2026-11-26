package use_case.online;

import javax.swing.SwingUtilities;

/** Runs HTTP work away from Swing's event-dispatch thread. */
final class OnlineUi {
  private OnlineUi() {
  }

  static void background(Runnable operation) {
    Thread thread = new Thread(operation, "passage-map-supabase-request");
    thread.setDaemon(true);
    thread.start();
  }

  static void present(Runnable operation) {
    SwingUtilities.invokeLater(operation);
  }
}
