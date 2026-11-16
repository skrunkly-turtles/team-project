package dataAccess;

import entity.Book;
import java.util.List;
import userCase.LookUpBookDataAccessInterface;

public class LibraryApiDataAccessObject implements LookUpBookDataAccessInterface {
    @Override
    public List<Book> searchByTitle(String title) {
        return null;
    }

    @Override
    public Book getById(String bookId) {
        return null;
    }
}
