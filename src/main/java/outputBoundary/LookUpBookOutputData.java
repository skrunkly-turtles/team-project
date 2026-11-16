package outputBoundary;

import java.util.List;

public class LookUpBookOutputData {
    private List<String> books;
    public LookUpBookOutputData(List<String> booksAsString) {
        this.books = booksAsString;
    }
    public List<String> getBooks() { return books; }
}
