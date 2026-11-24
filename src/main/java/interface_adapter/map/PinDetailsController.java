package interface_adapter.map;

import use_case.pin_details.PinDetailsInputBoundary;
import use_case.pin_details.PinCreationDraft.Visibility;
import use_case.select_location.SelectedLocation;

/** Routes form actions through the details input boundary. */
public class PinDetailsController {
    private final PinDetailsInputBoundary input;
    /**
     * Creates the form adapter.
     * @param input details input boundary
     */
    public PinDetailsController(PinDetailsInputBoundary input) { this.input = input; }
    /**
     * Opens the form for a confirmed location.
     * @param location selected geographic position
     */
    public void open(SelectedLocation location) { input.open(location); }
    /**
     * Passes current form values to the draft.
     * @param thoughts optional text
     * @param imagePath optional image reference
     * @param visibility selected audience, or null
     */
    public void update(String thoughts, String imagePath, Visibility visibility) { input.update(thoughts, imagePath, visibility); }
    /** Returns to the previous selection step. */
    public void back() { input.back(); }
    /** Discards the draft. */
    public void cancel() { input.cancel(); }
}
