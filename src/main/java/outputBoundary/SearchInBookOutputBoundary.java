package outputBoundary;

public interface SearchInBookOutputBoundary {
    void prepareSuccessView(SearchInBookOutputData outputData);

    void prepareFailView(String error);
}
