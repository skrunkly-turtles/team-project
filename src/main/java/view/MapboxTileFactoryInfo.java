package view;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jxmapviewer.viewer.TileFactoryInfo;

/** Adapts Mapbox street tiles to JXMapViewer's reversed zoom convention. */
public class MapboxTileFactoryInfo extends TileFactoryInfo {
    private final String accessToken;

    /**
     * Configures 256-pixel tiles at Mapbox zoom levels zero through nineteen.
     * @param token public Mapbox access token
     * @throws IllegalArgumentException when the token is missing
     */
    public MapboxTileFactoryInfo(String token) {
        super("Mapbox", 0, 19, 19, 256, true, true,
                "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256",
                "x", "y", "z");
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("A Mapbox access token is required.");
        }
        accessToken = token.trim();
    }

    /**
     * Converts viewer coordinates into an authenticated Mapbox tile URL.
     * @param x tile column
     * @param y tile row
     * @param zoom viewer zoom, where zero is most detailed
     * @return URL for the corresponding street tile
     */
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        return getBaseURL() + "/" + (19 - zoom) + "/" + x + "/" + y
                + "?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
    }
}
