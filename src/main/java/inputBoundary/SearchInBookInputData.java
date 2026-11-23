package inputBoundary;

/** What the user asked for: find this text inside the text of the book that is open. */
public class SearchInBookInputData {
    private final String bookText;
    private final String query;

    public SearchInBookInputData(String bookText, String query) {
        this.bookText = bookText;
        this.query = query;
    }

    public String getBookText() { return bookText; }
    public String getQuery() { return query; }
}
