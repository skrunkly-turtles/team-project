package view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies Mapbox coordinate adaptation without network requests. */
class MapboxTileFactoryInfoTest {
    /** Confirms both ends of the reversed zoom range and coordinate order. */
    @Test
    void convertsZoomAndCoordinates() {
        MapboxTileFactoryInfo info = new MapboxTileFactoryInfo("pk.example");
        assertTrue(info.getTileUrl(123, 456, 0).contains("/19/123/456?"));
        assertTrue(info.getTileUrl(0, 0, 19).contains("/0/0/0?"));
    }

    /** Prevents missing credentials from becoming tile requests. */
    @Test
    void rejectsMissingTokens() {
        assertThrows(IllegalArgumentException.class, () -> new MapboxTileFactoryInfo(null));
        assertThrows(IllegalArgumentException.class, () -> new MapboxTileFactoryInfo("  "));
    }

    /** Ensures token characters cannot introduce extra query parameters. */
    @Test
    void encodesToken() {
        assertTrue(new MapboxTileFactoryInfo(" pk.a&b ").getTileUrl(0, 0, 19)
                .endsWith("access_token=pk.a%26b"));
    }
}
