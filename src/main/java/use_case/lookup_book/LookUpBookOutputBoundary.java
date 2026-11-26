package use_case.lookup_book;

public interface LookUpBookOutputBoundary {
    void prepareSuccessView(LookUpBookOutputData outputData);

    void prepareFailView(String error);

    void prepareReaderView(LookUpBookReaderData readerData);

    void prepareOpenFailView(String error);
}
