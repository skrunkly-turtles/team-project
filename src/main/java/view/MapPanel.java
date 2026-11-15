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
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

/** Displays Mapbox imagery with drag, wheel, and button navigation. */
public class MapPanel extends JPanel {
    /**
     * Builds the interactive map, or setup instructions when no token is supplied.
     * @param token public Mapbox token supplied by application configuration
     */
    public MapPanel(String token) {
        super(new BorderLayout());
        if (token == null || token.trim().isEmpty()) {
            add(new JLabel("<html>Mapbox setup required.<br>Set MAPBOX_ACCESS_TOKEN "
                    + "in your IntelliJ run configuration, then restart.</html>"), BorderLayout.CENTER);
            return;
        }
        JXMapViewer mapViewer = new JXMapViewer();
        DefaultTileFactory tileFactory = new DefaultTileFactory(new MapboxTileFactoryInfo(token));
        tileFactory.setThreadPoolSize(4);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(8);
        mapViewer.setAddressLocation(new GeoPosition(43.6629, -79.3957));
        MouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        add(mapViewer, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton zoomIn = new JButton("+");
        zoomIn.setToolTipText("Zoom in");
        zoomIn.addActionListener(event -> mapViewer.setZoom(Math.max(0, mapViewer.getZoom() - 1)));
        JButton zoomOut = new JButton("−");
        zoomOut.setToolTipText("Zoom out");
        zoomOut.addActionListener(event -> mapViewer.setZoom(Math.min(19, mapViewer.getZoom() + 1)));
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
}
