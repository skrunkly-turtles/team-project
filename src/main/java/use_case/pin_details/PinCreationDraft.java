package use_case.pin_details;

import use_case.select_location.SelectedLocation;

/** Immutable, unsaved pin details; never writes or mutates a Pin entity. */
public final class PinCreationDraft {
    /** Audience choices to map to the shared pin representation when saving. */
    public enum Visibility { PUBLIC, PRIVATE, FRIENDS_ONLY }
    private final SelectedLocation location;
    private final SelectedPassage passage;
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
        this(location, null, thoughts, imagePath, visibility);
    }

    /** Captures the selected book/passage alongside the remaining unsaved fields.
     * @param location confirmed location
     * @param passage selected source metadata/text, or null before library completion
     * @param thoughts optional user text
     * @param imagePath optional local image reference
     * @param visibility selected audience, or null
     */
    public PinCreationDraft(SelectedLocation location, SelectedPassage passage, String thoughts,
            String imagePath, Visibility visibility) {
        this.location = location;
        this.passage = passage;
        this.thoughts = thoughts;
        this.imagePath = imagePath;
        this.visibility = visibility;
    }

    /** @return selected location */
    public SelectedLocation getLocation() { return location; }
    /** @return selected book and passage, or null */
    public SelectedPassage getPassage() { return passage; }
    /** @return optional thoughts */
    public String getThoughts() { return thoughts; }
    /** @return optional local image reference */
    public String getImagePath() { return imagePath; }
    /** @return chosen audience, or null */
    public Visibility getVisibility() { return visibility; }
}
