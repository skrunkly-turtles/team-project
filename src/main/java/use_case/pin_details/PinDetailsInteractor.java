package use_case.pin_details;

import java.util.Objects;
import use_case.select_location.SelectedLocation;
import use_case.pin_details.PinDetailsOutputData.Stage;

/** Maintains optional details and audience while the pin is unsaved. */
public class PinDetailsInteractor implements PinDetailsInputBoundary {
    private final PinDetailsOutputBoundary output;
    private PinCreationDraft draft;
    private Stage stage = Stage.CANCELLED;

    /**
     * Connects details results to presentation.
     * @param output output boundary
     */
    public PinDetailsInteractor(PinDetailsOutputBoundary output) { this.output = output; }

    /**
     * Opens or resumes details with the latest confirmed location.
     * @param location confirmed location, never null
     */
    @Override
    public void open(SelectedLocation location) {
        open(location, draft == null ? null : draft.getPassage());
    }

    /** Receives the completed library selection and retains the optional draft fields.
     * @param location latest confirmed location
     * @param passage immutable book/passage selection, or null before completion
     */
    @Override
    public void open(SelectedLocation location, SelectedPassage passage) {
        Objects.requireNonNull(location, "Choose a location before opening details.");
        draft = new PinCreationDraft(location, passage, draft == null ? "" : draft.getThoughts(),
                draft == null ? null : draft.getImagePath(), draft == null ? null : draft.getVisibility());
        stage = Stage.EDITING;
        output.present(new PinDetailsOutputData(draft, stage));
    }

    /**
     * Updates the current draft without requiring optional fields.
     * @param thoughts optional text, null is treated as empty
     * @param imagePath optional local reference
     * @param visibility chosen audience, null until explicitly selected
     */
    @Override
    public void update(String thoughts, String imagePath, PinCreationDraft.Visibility visibility) {
        if (stage != Stage.EDITING) { return; }
        draft = new PinCreationDraft(draft.getLocation(), draft.getPassage(), thoughts == null ? "" : thoughts,
                imagePath == null || imagePath.isBlank() ? null : imagePath, visibility);
        output.present(new PinDetailsOutputData(draft, stage));
    }

    /** Retains details when navigating to the previous step. */
    @Override
    public void back() {
        if (stage != Stage.EDITING) { return; }
        stage = Stage.BACK;
        output.present(new PinDetailsOutputData(draft, stage));
    }

    /** Clears the draft so the next pin starts empty. */
    @Override
    public void cancel() {
        if (stage == Stage.CANCELLED) { return; }
        draft = null;
        stage = Stage.CANCELLED;
        output.present(new PinDetailsOutputData(null, stage));
    }
}
