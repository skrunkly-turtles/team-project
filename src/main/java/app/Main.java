package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import view.HomeView;
import view.MapPanel;
import view.MapboxTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import interface_adapter.map.MapController;
import interface_adapter.map.MapPresenter;
import interface_adapter.map.MapViewModel;
import use_case.display_map.DisplayMapInteractor;

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
            // Assemble the map workflow and configure the tile provider.
            MapViewModel viewModel = new MapViewModel();
            MapPresenter presenter = new MapPresenter(viewModel);
            MapController controller = new MapController(new DisplayMapInteractor(presenter));
            String token = System.getenv("MAPBOX_ACCESS_TOKEN");
            DefaultTileFactory tileFactory = null;
            if (token != null && !token.trim().isEmpty()) {
                tileFactory = new DefaultTileFactory(new MapboxTileFactoryInfo(token));
                tileFactory.setThreadPoolSize(4);
            }
            MapPanel mapPanel = new MapPanel(tileFactory, viewModel);
            window.setContentPane(new HomeView(mapPanel, controller));
            window.setSize(1000, 700);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
