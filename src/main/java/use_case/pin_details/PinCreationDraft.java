package use_case.pin_details;

import use_case.select_location.SelectedLocation;

/** Immutable, unsaved pin details; never writes or mutates a Pin entity. */
public final class PinCreationDraft {
    /** Audience choices to map to the shared pin representation when saving. */
    public enum Visibility { PUBLIC, PRIVATE, FRIENDS_ONLY }
    private final SelectedLocation location;
    private final String thoughts;
    private final String imagePath;
    private final Visibility visibility;

    /**
     * Captures temporary creation data.
     * @param location chosen geographic position
     * @param thoughts optional user text
     * @param imagePath optional local image reference, not uploaded or persisted
     * @param visibility selected audience, or null until chosen
     */
    public PinCreationDraft(SelectedLocation location, String thoughts, String imagePath, Visibility visibility) {
        this.location = location;
        this.thoughts = thoughts;
        this.imagePath = imagePath;
        this.visibility = visibility;
    }

    /** @return selected location */
    public SelectedLocation getLocation() { return location; }
    /** @return optional thoughts */
    public String getThoughts() { return thoughts; }
    /** @return optional local image reference */
    public String getImagePath() { return imagePath; }
    /** @return chosen audience, or null */
    public Visibility getVisibility() { return visibility; }
}
