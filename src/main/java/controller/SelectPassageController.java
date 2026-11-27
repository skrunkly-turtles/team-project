package controller;

import inputBoundary.SelectPassageInputBoundary;
import inputBoundary.SelectPassageInputData;

public class SelectPassageController {
    private final SelectPassageInputBoundary interactor;

    public SelectPassageController(SelectPassageInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String bookId, int start, int end) {
        interactor.execute(new SelectPassageInputData(bookId, start, end));
    }
}
