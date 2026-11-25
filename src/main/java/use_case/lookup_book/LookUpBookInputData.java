package use_case.lookup_book;

public class LookUpBookInputData {
    private final String query;

    public LookUpBookInputData(String query) {
        this.query = query;
    }

    public String getQuery() { return query; }
}
