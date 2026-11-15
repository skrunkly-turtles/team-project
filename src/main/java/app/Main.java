package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import view.HomeView;
import view.MapPanel;

/** Starts and assembles the application. */
public class Main {

    /**
     * Opens the application window on Swing's event dispatch thread.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Passage Map");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new HomeView(new MapPanel(System.getenv("MAPBOX_ACCESS_TOKEN"))));
            window.setSize(1000, 700);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
