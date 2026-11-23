package outputBoundary;

/** What the reader screen needs to show one opened book. */
public class LookUpBookReaderData {
    private final String bookId;
    private final String title;
    private final String text;

    public LookUpBookReaderData(String title, String text) {
        this("", title, text);
    }

    // Optional constructor: also says which book this is, so the reader can search inside it and select passages.
    public LookUpBookReaderData(String bookId, String title, String text) {
        this.bookId = bookId;
        this.title = title;
        this.text = text;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getText() { return text; }
}
