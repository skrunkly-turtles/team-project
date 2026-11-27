package use_case.edit_pin;

public final class EditPinInputData {
    private final int pinId;
    private final String description;
    private final String latitude;
    private final String longitude;
    private final String passageText;
    private final String bookTitle;
    private final String author;
    private final String year;

    public EditPinInputData(int pinId, String description, String latitude,
                            String longitude, String passageText, String bookTitle, String author, String year) {
        this.pinId = pinId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.passageText = passageText;
        this.bookTitle = bookTitle;
        this.author = author;
        this.year = year;
    }

    public int getPinId() { return pinId; }
    public String getDescription() { return description; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public String getPassageText() { return passageText; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthor() { return author; }
    public String getYear() { return year; }
}