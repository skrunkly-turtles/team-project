package views;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookState {
    private List<String> results = new ArrayList<>();
    private String error = "";

    public LookUpBookState(List<String> results, String error) {
        this.results = results;
        this.error = error;
    }

    public List<String> getResults() { return results; }
    public String getError() { return error; }
    public void setResults(List<String> results) { this.results = results; }
    public void setError(String error) { this.error = error; }


}
