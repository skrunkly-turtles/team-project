package use_case.lookup_book;

public interface LookUpBookInputBoundary {
    void execute(LookUpBookInputData lookUpBookInputData);

    void openBook(String bookId);
}
