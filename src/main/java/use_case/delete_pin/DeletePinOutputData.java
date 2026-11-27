package use_case.delete_pin;

public final class DeletePinOutputData {
    private final String username;
    private final int pinId;

    public DeletePinOutputData(String username, int pinId) {
        this.username = username;
        this.pinId = pinId;
    }

    public String getUsername() { return username; }
    public int getPinId() { return pinId; }
}