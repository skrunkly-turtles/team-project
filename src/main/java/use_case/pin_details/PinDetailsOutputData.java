package use_case.pin_details;

/** Snapshot of the details screen's draft and navigation stage. */
public final class PinDetailsOutputData {
    /** Navigation stages of the details preview. */
    public enum Stage { EDITING, BACK, CANCELLED }
    private final PinCreationDraft draft;
    private final Stage stage;
    /**
     * Creates a presentation result.
     * @param draft current immutable draft, or null after cancellation
     * @param stage navigation stage
     */
    public PinDetailsOutputData(PinCreationDraft draft, Stage stage) {
        this.draft = draft;
        this.stage = stage;
    }
    /** @return current draft, or null */
    public PinCreationDraft getDraft() { return draft; }
    /** @return current navigation stage */
    public Stage getStage() { return stage; }
}
