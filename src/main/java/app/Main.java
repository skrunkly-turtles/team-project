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
import use_case.select_location.SelectLocationInteractor;
import interface_adapter.map.LocationSelectionController;
import interface_adapter.map.LocationSelectionPresenter;
import interface_adapter.map.LocationSelectionViewModel;

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
            LocationSelectionViewModel selectionViewModel = new LocationSelectionViewModel();
            LocationSelectionPresenter selectionPresenter = new LocationSelectionPresenter(
                    selectionViewModel, selectedLocation -> {
                        // Connect this callback to book selection once the shared handoff is agreed.
                        // The confirmed location remains in selectionViewModel until cancellation.
                    });
            LocationSelectionController selectionController = new LocationSelectionController(
                    new SelectLocationInteractor(selectionPresenter));
            mapPanel.enableLocationSelection(selectionController, selectionViewModel);
            window.setContentPane(new HomeView(mapPanel, controller));
            window.setSize(1000, 700);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
