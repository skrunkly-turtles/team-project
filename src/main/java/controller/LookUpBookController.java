package controller;

import inputBoundary.LookUpBookInputBoundary;
import inputBoundary.LookUpBookInputData;

/*
    User Agent: CSC207 Library App (taysen.victory@mail.utoronto.ca)
    Open Library: Book Search, Search Inside
    https://openlibrary.org/dev/docs/api/search

    https://openlibrary.org/search.json?q=the+lord+of+the+rings
 */
public class LookUpBookController {
    private final LookUpBookInputBoundary interactor;

    public LookUpBookController(LookUpBookInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String query) {
        LookUpBookInputData inputData = new LookUpBookInputData(query);
        interactor.execute(inputData);
    }

    public void openBook(String bookId) {
        interactor.openBook(bookId);
    }
}
