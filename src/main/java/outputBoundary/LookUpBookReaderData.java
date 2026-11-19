package outputBoundary;

/** What the reader screen needs to show one opened book. */
public class LookUpBookReaderData {
    private final String title;
    private final String text;

    public LookUpBookReaderData(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() { return title; }
    public String getText() { return text; }
}
