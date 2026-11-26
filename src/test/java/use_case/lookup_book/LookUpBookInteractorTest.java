package use_case.lookup_book;

import models.Book;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LookUpBookInteractorTest {
    private static class FakeDao implements LookUpBookDataAccessInterface {
        private final List<Book> books;
        boolean shouldThrow = false;

        FakeDao(List<Book> books) { this.books = books; }

        @Override public List<Book> searchByTitle(String title) {
            if (shouldThrow) {
                throw new RuntimeException("Could not reach openlibrary.org");
            }
            return books;
        }
        @Override public Book getById(String bookId) {
            if (shouldThrow) {
                throw new RuntimeException("Could not reach archive.org");
            }
            for (Book book : books) {
                if (book.getId().equals(bookId)) {
                    return book;
                }
            }
            return null;   // unknown id
        }
    }

    private static class RecordingPresenter implements LookUpBookOutputBoundary {
        LookUpBookOutputData success;
        String failMessage;
        LookUpBookReaderData readerData;
        String openFailMessage;

        @Override public void prepareSuccessView(LookUpBookOutputData data) { success = data; }
        @Override public void prepareFailView(String error) { failMessage = error; }
        @Override public void prepareReaderView(LookUpBookReaderData data) { readerData = data; }
        @Override public void prepareOpenFailView(String error) { openFailMessage = error; }
    }

    private static void assertContains(String expected, String actual) {
        assertNotNull(actual, "Expected a message containing \"" + expected + "\" but got null");
        assertTrue(actual.contains(expected),
                "Expected \"" + actual + "\" to contain \"" + expected + "\"");
    }

    @Test
    void searchWithResults_sendsToPresenter() {
        Book book = new Book("Emma", "Jane Austen", 1815, "emma1");
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(List.of(book)), presenter);

        interactor.execute(new LookUpBookInputData("emma"));

        assertNull(presenter.failMessage);
        assertEquals(List.of("Emma by Jane Austen (1815)"), presenter.success.getBooks());
        assertEquals(List.of("emma1"), presenter.success.getBookIds());

    }

    @Test
    void emptySearch_throwsFailure() {
        Book book = new Book("Emma", "Jane Austen", 1815, "emma1");
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(List.of(book)), presenter);

        interactor.execute(new LookUpBookInputData("     "));

        assertEquals("Please enter a book title", presenter.failMessage);
        assertNull(presenter.success);
    }

    @Test
    void searchWithNoResults_throwsFailure() {
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(new ArrayList<>()), presenter);

        interactor.execute(new LookUpBookInputData("Emma"));

        assertEquals("No books found", presenter.failMessage);
        assertNull(presenter.success);
    }

    @Test
    void cantConnect_presenterGetsFailMessage() {
        FakeDao dao = new FakeDao(List.of());
        dao.shouldThrow = true;
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor = new LookUpBookInteractor(dao, presenter);

        interactor.execute(new LookUpBookInputData("emma"));

        assertNull(presenter.success);
        assertNotNull(presenter.failMessage);
        assertContains("Could not reach", presenter.failMessage);
    }

    @Test
    void searchWithSeveralResults_keepsListsInSync() {
        Book pride = new Book("Pride and Prejudice", "Jane Austen", 1813, "prideprejudice00aust");
        Book power = new Book("Power in Global Governance", "Michael Barnett", 2005, "poweringlobalgov5");
        Book taekwondo = new Book("Taekwondo: Ancient Wisdom for the Modern Warrior", "Doug Cook", 2001, "taekwondo00cook");
        Book wonder = new Book("Wonder", "R. J. Palacio", 2012, "wonder0000pala");
        Book zen = new Book("Zen and the Art of Motorcycle Maintenance", "Robert M. Pirsig", 1974, "zenartofmotorcyc00pirs");
        Book chasingShadows = new Book("Chasing Shadows", "Ron Deibert", 2025, "deibert25shadows");
        Book nineteenEightyFour = new Book("1984", "George Orwell", 1949, "nineteeneightyfo00orwe");
        Book fahrenheit = new Book("Fahrenheit 451", "Ray Bradbury", 1953, "fahrenheit451000brad");
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor = new LookUpBookInteractor(
                new FakeDao(List.of(pride, power, taekwondo, wonder, zen, chasingShadows, nineteenEightyFour, fahrenheit)),
                presenter);

        interactor.execute(new LookUpBookInputData("power"));

        assertEquals(List.of("Pride and Prejudice by Jane Austen (1813)",
                "Power in Global Governance by Michael Barnett (2005)",
                "Taekwondo: Ancient Wisdom for the Modern Warrior by Doug Cook (2001)",
                "Wonder by R. J. Palacio (2012)",
                "Zen and the Art of Motorcycle Maintenance by Robert M. Pirsig (1974)",
                "Chasing Shadows by Ron Deibert (2025)",
                "1984 by George Orwell (1949)",
                "Fahrenheit 451 by Ray Bradbury (1953)"), presenter.success.getBooks());
        assertEquals(List.of("prideprejudice00aust", "poweringlobalgov5", "taekwondo00cook",
                "wonder0000pala", "zenartofmotorcyc00pirs", "deibert25shadows",
                "nineteeneightyfo00orwe", "fahrenheit451000brad"), presenter.success.getBookIds());
    }

    @Test
    void openBook_sendsTitleAndTextToPresenter() {
        Book pride = new Book("Pride and Prejudice", "Jane Austen", 1813, "prideprejudice00aust");
        String text = "\"I must think your language too strong in speaking of both,\" replied Jane; \"and I hope you will be convinced of it by seeing them happy together. But enough of this. You alluded to something else. ";
        pride.setText(text);
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(List.of(pride)), presenter);

        interactor.openBook("prideprejudice00aust");

        assertEquals("Pride and Prejudice", presenter.readerData.getTitle());
        assertEquals(pride.getText(), presenter.readerData.getText());
        assertEquals(text, pride.getText());
        assertNull(presenter.openFailMessage);
    }

    @Test
    void openBookWithBlankId_failsWithSelectFirstMessage() {
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(new ArrayList<>()), presenter);

        interactor.openBook("   ");

        assertEquals("Select a book first", presenter.openFailMessage);
        assertNull(presenter.readerData);
    }

    @Test
    void openBookWithNullId_failsWithSelectFirstMessage() {
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(new ArrayList<>()), presenter);

        interactor.openBook(null);

        assertEquals("Select a book first", presenter.openFailMessage);
        assertNull(presenter.readerData);
    }

    @Test
    void openBookWithNoText_failsWithNotAvailableMessage() {
        Book power = new Book("Power in Global Governance", "Michael Barnett", 2005, "poweringlobalgov0000unse");
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(List.of(power)), presenter);

        interactor.openBook("poweringlobalgov0000unse");

        assertEquals("No text is available for this book", presenter.openFailMessage);
        assertNull(presenter.readerData);
    }

    @Test
    void openBookWithUnknownId_failsWithNotAvailableMessage() {
        Book pride = new Book("Pride and Prejudice", "Jane Austen", 1813, "prideprejudice00aust");
        pride.setText("I must think your language too strong in speaking of both");
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor =
                new LookUpBookInteractor(new FakeDao(List.of(pride)), presenter);

        interactor.openBook("not-a-real-id");

        assertEquals("No text is available for this book", presenter.openFailMessage);
        assertNull(presenter.readerData);
    }

    @Test
    void openBookCantConnect_presenterGetsOpenFailMessage() {
        Book pride = new Book("Pride and Prejudice", "Jane Austen", 1813, "prideprejudice00aust");
        FakeDao dao = new FakeDao(List.of(pride));
        dao.shouldThrow = true;
        RecordingPresenter presenter = new RecordingPresenter();
        LookUpBookInteractor interactor = new LookUpBookInteractor(dao, presenter);

        interactor.openBook("prideprejudice00aust");

        assertNull(presenter.readerData);
        assertContains("Could not load", presenter.openFailMessage);
    }
}
