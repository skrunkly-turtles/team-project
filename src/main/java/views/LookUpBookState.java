package views;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookState {
    private List<String> results = new ArrayList<>();
    private List<String> bookIds = new ArrayList<>();
    private String error = "";

    public LookUpBookState(List<String> results, String error) {
        this.results = results;
        this.error = error;
    }

    // Optional constructor: also keeps each result's book id so the chosen book can be opened.
    public LookUpBookState(List<String> results, List<String> bookIds, String error) {
        this.results = results;
        this.bookIds = bookIds;
        this.error = error;
    }

    public List<String> getResults() { return results; }
    public List<String> getBookIds() { return bookIds; }
    public String getError() { return error; }
    public void setResults(List<String> results) { this.results = results; }
    public void setBookIds(List<String> bookIds) { this.bookIds = bookIds; }
    public void setError(String error) { this.error = error; }


}
