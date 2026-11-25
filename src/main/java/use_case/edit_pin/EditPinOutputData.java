package use_case.edit_pin;

public final class EditPinOutputData {
    private final String username;
    private final int pinId;

    public EditPinOutputData(String username, int pinId) {
        this.username = username;
        this.pinId = pinId;
    }

    public String getUsername() { return username; }
    public int getPinId() { return pinId; }
}