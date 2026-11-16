package view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.TileFactory;
import interface_adapter.map.MapViewModel;
import org.jxmapviewer.viewer.GeoPosition;

/** Displays Mapbox imagery with drag, wheel, and button navigation. */
public class MapPanel extends JPanel {
    private final JXMapViewer mapViewer;
    /**
     * Builds the interactive map, or setup instructions when no token is supplied.
     * @param tileFactory configured map provider, or null when setup is missing
     * @param viewModel provider-independent display state updated by the presenter
     */
    public MapPanel(TileFactory tileFactory, MapViewModel viewModel) {
        super(new BorderLayout());
        if (tileFactory == null) {
            mapViewer = null;
            add(new JLabel("<html>Mapbox setup required.<br>Set MAPBOX_ACCESS_TOKEN "
                    + "in your IntelliJ run configuration, then restart.</html>"), BorderLayout.CENTER);
            return;
        }
        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        viewModel.addPropertyChangeListener(event -> {
            // Only the rendering layer knows the widget's reversed zoom convention.
            mapViewer.setZoom(tileFactory.getInfo().getTotalMapZoom() - viewModel.getZoom());
            mapViewer.setAddressLocation(new GeoPosition(viewModel.getLatitude(), viewModel.getLongitude()));
        });
        MouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        add(mapViewer, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton zoomIn = new JButton("+");
        zoomIn.setToolTipText("Zoom in");
        zoomIn.addActionListener(event -> mapViewer.setZoom(Math.max(tileFactory.getInfo().getMinimumZoomLevel(), mapViewer.getZoom() - 1)));
        JButton zoomOut = new JButton("−");
        zoomOut.setToolTipText("Zoom out");
        zoomOut.addActionListener(event -> mapViewer.setZoom(Math.min(tileFactory.getInfo().getMaximumZoomLevel(), mapViewer.getZoom() + 1)));
        controls.add(zoomIn);
        controls.add(zoomOut);
        controls.add(new JLabel("Drag to move | Scroll to zoom"));
        // Keep the controls above the map at its bottom-right edge as it resizes.
        mapViewer.setLayout(new GridBagLayout());
        GridBagConstraints controlPosition = new GridBagConstraints();
        controlPosition.weightx = 1.0;
        controlPosition.weighty = 1.0;
        controlPosition.anchor = GridBagConstraints.SOUTHEAST;
        controlPosition.insets = new Insets(12, 12, 12, 12);
        mapViewer.add(controls, controlPosition);
        JPanel attribution = new JPanel(new BorderLayout());
        attribution.add(new JLabel(new ImageIcon(
                MapPanel.class.getResource("/mapbox-logo.png"))), BorderLayout.WEST);
        JEditorPane credits = new JEditorPane("text/html",
                "<html><a href='https://www.mapbox.com/about/maps/'>© Mapbox</a> | "
                + "<a href='https://www.openstreetmap.org/copyright'>© OpenStreetMap</a> | "
                + "<a href='https://apps.mapbox.com/feedback/'>Improve this map</a></html>");
        credits.setEditable(false);
        credits.setOpaque(false);
        credits.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(URI.create(event.getURL().toString()));
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(this, "Open this link in your browser: "
                            + event.getURL(), "Map attribution", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        attribution.add(credits, BorderLayout.CENTER);
        add(attribution, BorderLayout.SOUTH);
    }

    /**
     * Adds the location-selection entry point while the sidebar is developed separately.
     * @param controller selection request adapter
     * @param selectionViewModel state for the temporary marker
     */
    public void enableLocationSelection(
            interface_adapter.map.LocationSelectionController controller,
            interface_adapter.map.LocationSelectionViewModel selectionViewModel) {
        if (mapViewer != null) {
            add(new LocationSelectionPanel(mapViewer, controller, selectionViewModel), BorderLayout.NORTH);
        }
    }

}
