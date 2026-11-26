package use_case.lookup_book;

import models.Book;
import java.util.List;

public interface LookUpBookDataAccessInterface {
    List<Book> searchByTitle(String title);

    Book getById(String bookId);
}
