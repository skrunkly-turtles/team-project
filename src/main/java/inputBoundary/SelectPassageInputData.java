package inputBoundary;

/** What the user asked for: keep the part of the open book between these two positions. */
public class SelectPassageInputData {
    private final String bookId;
    private final int start;   // position of the first selected character
    private final int end;     // position just after the last selected character

    public SelectPassageInputData(String bookId, int start, int end) {
        this.bookId = bookId;
        this.start = start;
        this.end = end;
    }

    public String getBookId() { return bookId; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
}
