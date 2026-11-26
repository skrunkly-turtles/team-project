package use_case.lookup_book;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookOutputData {
    private List<String> books;
    private List<String> bookIds = new ArrayList<>();

    public LookUpBookOutputData(List<String> booksAsString) {
        this.books = booksAsString;
    }

    // Optional constructor: also carries each book's id so a chosen book can be opened later.
    public LookUpBookOutputData(List<String> booksAsString, List<String> bookIds) {
        this.books = booksAsString;
        this.bookIds = bookIds;
    }

    public List<String> getBooks() { return books; }
    public List<String> getBookIds() { return bookIds; }
}
