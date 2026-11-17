package view;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;
import interface_adapter.map.LocationSelectionController;
import interface_adapter.map.LocationSelectionViewModel;
import use_case.select_location.SelectedLocation;
import use_case.select_location.SelectLocationOutputData;
import use_case.select_location.SelectLocationOutputData.Stage;

/** Controls an unsaved marker and adapts map gestures to geographic coordinates. */
public class LocationSelectionPanel extends JPanel {
    /**
     * Connects selection controls and marker rendering to the map.
     * @param viewer interactive map widget
     * @param controller selection input adapter
     * @param viewModel observable selection state
     */
    public LocationSelectionPanel(JXMapViewer viewer, LocationSelectionController controller,
                                  LocationSelectionViewModel viewModel) {
        JButton confirm = new JButton("Continue");
        JButton cancel = new JButton("Cancel");
        JLabel status = new JLabel("Choose a location for a new pin.");
        confirm.setEnabled(false);
        cancel.setEnabled(false);
        setVisible(false);
        add(confirm);
        add(cancel);
        add(status);
        confirm.addActionListener(event -> controller.confirm());
        cancel.addActionListener(event -> controller.cancel());
        WaypointPainter<DefaultWaypoint> markerPainter = new WaypointPainter<>();
        viewer.setOverlayPainter(markerPainter);
        viewModel.addPropertyChangeListener(event -> {
            SelectLocationOutputData state = viewModel.getState();
            boolean selecting = state.getStage() == Stage.SELECTING;
            SelectedLocation location = state.getLocation();
            setVisible(selecting);
            revalidate();
            confirm.setEnabled(selecting && location != null);
            cancel.setEnabled(state.getStage() != Stage.INACTIVE);
            if (location == null) {
                markerPainter.setWaypoints(Collections.emptySet());
            } else {
                markerPainter.setWaypoints(Collections.singleton(new DefaultWaypoint(
                        location.getLatitude(), location.getLongitude())));
            }
            if (!state.getError().isEmpty()) {
                status.setText(state.getError());
            } else if (state.getStage() == Stage.CONFIRMED) {
                status.setText("Location ready; book selection connection pending.");
            } else if (location != null) {
                status.setText(String.format(Locale.ROOT, "Selected: %.5f, %.5f",
                        location.getLatitude(), location.getLongitude()));
            } else {
                status.setText(selecting ? "Click the map to choose a location."
                        : "Choose a location for a new pin.");
            }
            viewer.repaint();
        });
        viewer.addMouseListener(new MouseAdapter() {
            private Point pressed;

            /** Records left-button presses so map dragging is not treated as selection. */
            @Override
            public void mousePressed(MouseEvent event) {
                pressed = SwingUtilities.isLeftMouseButton(event) ? event.getPoint() : null;
            }

            /** Converts a left click to latitude/longitude while selection is active. */
            @Override
            public void mouseReleased(MouseEvent event) {
                if (pressed != null && SwingUtilities.isLeftMouseButton(event)
                        && pressed.distance(event.getPoint()) <= 5
                        && viewModel.getState().getStage() == Stage.SELECTING) {
                    GeoPosition position = viewer.convertPointToGeoPosition(event.getPoint());
                    double longitude = ((position.getLongitude() + 180) % 360 + 360) % 360 - 180;
                    controller.select(position.getLatitude(), longitude);
                }
                pressed = null;
            }
        });
    }
}
