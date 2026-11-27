package userCase;

import models.Book;
import models.Passage;
import inputBoundary.SelectPassageInputBoundary;
import inputBoundary.SelectPassageInputData;
import outputBoundary.SelectPassageOutputBoundary;
import outputBoundary.SelectPassageOutputData;

public class SelectPassageInteractor implements SelectPassageInputBoundary {
    static final int MAX_WORDS = 250;   // a passage may not be longer than this

    private final SelectPassageDataAccessInterface dataAccess;
    private final SelectPassageOutputBoundary presenter;

    public SelectPassageInteractor(SelectPassageDataAccessInterface dataAccess, SelectPassageOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(SelectPassageInputData inputData) {
        Book book;
        try {
            book = dataAccess.getById(inputData.getBookId());
        } catch (RuntimeException e) {
            presenter.prepareFailView("Could not load this book, please open it again");
            return;
        }
        if (book == null || book.getText().isEmpty()) {
            presenter.prepareFailView("No text is available for this book");
            return;
        }

        String text = book.getText();
        int start = inputData.getStart();
        int end = inputData.getEnd();
        if (start < 0 || end > text.length() || start > end) {
            presenter.prepareFailView("The selection is outside the book, please select the text again");
            return;
        }

        // Cut the passage out of the book's own text, so it always matches what the book says.
        String passageText = text.substring(start, end).trim();
        int wordCount = countWords(passageText);
        if (wordCount == 0) {
            presenter.prepareFailView("Select some text first");
            return;
        }
        if (wordCount > MAX_WORDS) {
            presenter.prepareFailView("Selection is " + wordCount + " words, the limit is " + MAX_WORDS);
            return;
        }

        dataAccess.savePassage(new Passage(book, passageText, wordCount));
        presenter.prepareSuccessView(new SelectPassageOutputData(passageText, wordCount));
    }

    /** Number of words in the text: groups of characters separated by whitespace. */
    static int countWords(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }
}
