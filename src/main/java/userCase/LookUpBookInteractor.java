package userCase;

import models.Book;
import inputBoundary.LookUpBookInputBoundary;
import inputBoundary.LookUpBookInputData;
import outputBoundary.LookUpBookOutputBoundary;
import outputBoundary.LookUpBookOutputData;
import outputBoundary.LookUpBookReaderData;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookInteractor implements LookUpBookInputBoundary {
    private final LookUpBookDataAccessInterface dataAccess;
    private final LookUpBookOutputBoundary presenter;

    public LookUpBookInteractor(LookUpBookDataAccessInterface dataAccess, LookUpBookOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }
    @Override
    public void execute(LookUpBookInputData inputData) {
        String query = inputData.getQuery();
        if (query == null || query.isBlank()) {
            presenter.prepareFailView("Please enter a book title");
            return;
        }

        List<Book> books;
        try {
            books = dataAccess.searchByTitle(query);
        } catch (RuntimeException e) {
            presenter.prepareFailView("Could not reach the library, please try again");
            return;
        }

        if (books.isEmpty()) {
            presenter.prepareFailView("No books found");
            return;
        }

        List<String> booksAsString = new ArrayList<>();
        List<String> bookIds = new ArrayList<>();
        for (Book book : books) {
            booksAsString.add(book.toString());
            bookIds.add(book.getId());
        }

        presenter.prepareSuccessView(new LookUpBookOutputData(booksAsString, bookIds));
//        openBook(query);
    }

    @Override
    public void openBook(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            presenter.prepareOpenFailView("Select a book first");
            return;
        }

        Book book;
        try {
            book = dataAccess.getById(bookId);
        } catch (RuntimeException e) {
            presenter.prepareOpenFailView("Could not load this book's text, please try again");
            return;
        }

        if (book == null || book.getText().isEmpty()) {
            presenter.prepareOpenFailView("No text is available for this book");
            return;
        }

        presenter.prepareReaderView(new LookUpBookReaderData(book.getTitle(), book.getText()));
    }
}
