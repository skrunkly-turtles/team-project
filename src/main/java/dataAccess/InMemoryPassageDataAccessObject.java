package dataAccess;

import models.Book;
import models.Passage;
import userCase.LookUpBookDataAccessInterface;
import userCase.SelectPassageDataAccessInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps selected passages in memory (they are gone when the app closes).
 * Books are looked up through the book data access we already have, so only one place knows about the library.
 */
public class InMemoryPassageDataAccessObject implements SelectPassageDataAccessInterface {
    private final LookUpBookDataAccessInterface books;
    private final List<Passage> passages = new CopyOnWriteArrayList<>();   // safe: the view saves from a background thread

    public InMemoryPassageDataAccessObject(LookUpBookDataAccessInterface books) {
        this.books = books;
    }

    @Override
    public Book getById(String bookId) { return books.getById(bookId); }

    @Override
    public void savePassage(Passage passage) { passages.add(passage); }

    @Override
    public List<Passage> getPassages() { return new ArrayList<>(passages); }
}
