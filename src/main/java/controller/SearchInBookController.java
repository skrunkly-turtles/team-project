package controller;

import inputBoundary.SearchInBookInputBoundary;
import inputBoundary.SearchInBookInputData;

public class SearchInBookController {
    private final SearchInBookInputBoundary interactor;

    public SearchInBookController(SearchInBookInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String bookText, String query) {
        interactor.execute(new SearchInBookInputData(bookText, query));
    }
}
