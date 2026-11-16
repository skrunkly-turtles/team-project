package userCase;

import entity.Book;
import java.util.List;

public interface LookUpBookDataAccessInterface {
    List<Book> searchByTitle(String title);

    Book getById(String bookId);
}
