package userCase;

import controller.LookUpBookController;
import dataAccess.LibraryApiDataAccessObject;
import entity.Book;
import inputBoundary.LookUpBookInputBoundary;
import inputBoundary.LookUpBookInputData;
import outputBoundary.LookUpBookOutputBoundary;
import outputBoundary.LookUpBookOutputData;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookInteractor implements LookUpBookInputBoundary {
    private LookUpBookDataAccessInterface dataAccess;
    private LookUpBookOutputBoundary presenter;

    public LookUpBookInteractor(LookUpBookDataAccessInterface dataAccess, LookUpBookOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }
    @Override
    public void execute(LookUpBookInputData inputData) {
        String query = inputData.getQuery();
        List<Book> books =  dataAccess.searchByTitle(query);
        List<String> booksAsString = new ArrayList<>();
        for (Book book : books) {
            booksAsString.add(book.toString());
        }

        presenter.prepareSuccessView(new LookUpBookOutputData(booksAsString));
//        openBook(query);
    }

    @Override
    public void openBook(String bookName) {
        dataAccess = new LibraryApiDataAccessObject();
        List<Book> books =  dataAccess.searchByTitle(bookName);
    }
}
