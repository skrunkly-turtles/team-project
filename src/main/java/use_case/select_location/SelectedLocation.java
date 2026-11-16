package use_case.select_location;

/** Immutable coordinates carried between pin-creation steps. */
public final class SelectedLocation {
    private final double latitude;
    private final double longitude;

    /**
     * Stores a validated location passed by the selection use case.
     * @param latitude latitude in degrees
     * @param longitude longitude in degrees
     */
    public SelectedLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** @return latitude in degrees */
    public double getLatitude() { return latitude; }

    /** @return longitude in degrees */
    public double getLongitude() { return longitude; }
}
