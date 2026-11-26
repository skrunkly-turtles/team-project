package interface_adapter.add_friend;

/** Immutable, non-sensitive display state for the add-friend widget. */
public final class AddFriendState {
    private final String message;
    private final boolean success;

    /** Creates the initial state, with no message. */
    public AddFriendState() {
        this("", false);
    }

    /** Creates a state describing the latest outcome. */
    public AddFriendState(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    /** Returns the message to show, or an empty string. */
    public String getMessage() {
        return message;
    }

    /** Returns whether the latest request succeeded. */
    public boolean isSuccess() {
        return success;
    }
}