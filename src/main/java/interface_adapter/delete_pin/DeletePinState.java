package interface_adapter.delete_pin;

/** Immutable display state for the latest delete attempt. */
public final class DeletePinState {
    private final String message;
    private final boolean success;

    public DeletePinState() {
        this("", false);
    }

    public DeletePinState(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
}