package use_case.delete_pin;

/** Identifies which pin the signed-in user wants to delete. */
public final class DeletePinInputData {
    private final int pinId;

    /** Creates a request without including an untrusted owner username. */
    public DeletePinInputData(int pinId) {
        this.pinId = pinId;
    }

    public int getPinId() { return pinId; }
}