package interface_adapter.edit_pin;

/** Immutable display state for the edit-pin form. */
public final class EditPinState {
    private final String message;
    private final boolean success;

    public EditPinState() {
        this("", false);
    }

    public EditPinState(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
}