package use_case.pin_details;

import use_case.select_location.SelectedLocation;

/** Requests for the unsaved details step. */
public interface PinDetailsInputBoundary {
    /**
     * Opens details at a confirmed location, retaining entries after Back.
     * @param location confirmed location
     */
    void open(SelectedLocation location);
    /**
     * Replaces the optional fields and audience in the current draft.
     * @param thoughts optional text
     * @param imagePath optional local image reference
     * @param visibility selected audience, or null
     */
    void update(String thoughts, String imagePath, PinCreationDraft.Visibility visibility);
    /** Returns to location selection while retaining draft entries. */
    void back();
    /** Discards all temporary details. */
    void cancel();
}
