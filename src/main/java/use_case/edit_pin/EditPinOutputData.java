package use_case.edit_pin;

public final class EditPinOutputData {
    private final String username;
    private final int pinId;
    private final String name;

    public EditPinOutputData(String username, int pinId, String name) {
        this.username = username;
        this.pinId = pinId;
        this.name = name;
    }

    public String getUsername() { return username; }
    public int getPinId() { return pinId; }
    public String getName() { return name; }
}