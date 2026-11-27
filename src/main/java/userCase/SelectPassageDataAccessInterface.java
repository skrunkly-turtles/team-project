package userCase;

import models.Book;
import models.Passage;

import java.util.List;

public interface SelectPassageDataAccessInterface {
    Book getById(String bookId);

    void savePassage(Passage passage);

    List<Passage> getPassages();
}
