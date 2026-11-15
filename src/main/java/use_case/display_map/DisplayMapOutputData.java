package use_case.display_map;

/** Immutable, provider-independent result of opening the home map. */
public class DisplayMapOutputData {
    private final double latitude;
    private final double longitude;
    private final int zoom;

    /**
     * Stores a geographic viewport without UI or provider objects.
     * @param latitude center latitude in degrees
     * @param longitude center longitude in degrees
     * @param zoom conventional slippy-map zoom, increasing with detail
     */
    public DisplayMapOutputData(double latitude, double longitude, int zoom) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
    }

    /** @return center latitude in degrees */
    public double getLatitude() { return latitude; }

    /** @return center longitude in degrees */
    public double getLongitude() { return longitude; }

    /** @return conventional geographic zoom level */
    public int getZoom() { return zoom; }
}
