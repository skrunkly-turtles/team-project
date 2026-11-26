package integration;

import interface_adapter.ViewManagerModel;
import interface_adapter.lookup_book.LookUpBookController;
import models.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import interface_adapter.lookup_book.LookUpBookPresenter;
import use_case.lookup_book.LookUpBookDataAccessInterface;
import use_case.lookup_book.LookUpBookInteractor;
import interface_adapter.lookup_book.LookUpBookState;
import view.LookUpBookView;
import interface_adapter.lookup_book.ReaderState;
import view.ReaderView;
import view.ViewManager;
import interface_adapter.lookup_book.LookupBookViewModel;
import models.ViewNames;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the controller, interactor, presenter, view models and screens together. Data access is faked
 * to avoid making API calls
 */
class LookUpBookIntegrationTest {
    private static final String PRIDE_ID = "prideprejudice00aust";
    private static final String POWER_ID = "poweringlobalgov5";
    private static final String SHADOWS_ID = "deibert25shadows";
    private static final String PRIDE_TEXT = "\"I must think your language too strong in speaking of both,\" replied Jane; \"and I hope you will be convinced of it by seeing them happy together. But enough of this. You alluded to something else. ";

    private static class FakeDao implements LookUpBookDataAccessInterface {
        private final List<Book> books;
        boolean shouldThrow = false;
        int searches = 0;

        FakeDao(List<Book> books) { this.books = books; }

        @Override public List<Book> searchByTitle(String title) {
            searches++;
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
            return null;
        }
    }

    private FakeDao dao;
    private LookupBookViewModel<LookUpBookState> searchViewModel;
    private LookupBookViewModel<ReaderState> readerViewModel;
    private ViewManagerModel navViewModel;
    private LookUpBookController controller;

    @BeforeEach
    void setUp() {
        Book pride = new Book("Pride and Prejudice", "Jane Austen", 1813, PRIDE_ID);
        pride.setText(PRIDE_TEXT);
        Book power = new Book("Power in Global Governance", "Michael Barnett", 2005, POWER_ID);   // no text yet
        Book shadows = new Book("Preventing “Stupid” Deaths: A Comparative Analysis of Policy Failures in Pandemic Response",
                "Joy Fitzgibbon", 2050, SHADOWS_ID);
        shadows.setText(PRIDE_TEXT);
        dao = new FakeDao(List.of(pride, power, shadows));

        searchViewModel = new LookupBookViewModel<>(new LookUpBookState(new ArrayList<>(), ""));
        readerViewModel = new LookupBookViewModel<>(new ReaderState());
        navViewModel = new ViewManagerModel();
        LookUpBookPresenter presenter = new LookUpBookPresenter(searchViewModel, readerViewModel, navViewModel);
        controller = new LookUpBookController(new LookUpBookInteractor(dao, presenter));
    }

    private static void waitForSwing() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> { });
    }

    @Test
    void searchWithResults_fillsStateWithEntryAndIdPerBook() {
        controller.execute("power");

        LookUpBookState state = searchViewModel.getState();
        assertEquals("", state.getError());
        assertEquals("Pride and Prejudice by Jane Austen (1813)", state.getResults().get(0));
        assertEquals("Power in Global Governance by Michael Barnett (2005)", state.getResults().get(1));
        assertEquals(List.of(PRIDE_ID, POWER_ID, SHADOWS_ID), state.getBookIds());
    }

    @Test
    void searchWithLongTitle_isShortenedButKeepsYearAndId() {
        controller.execute("pandemic");

        LookUpBookState state = searchViewModel.getState();
        String shown = state.getResults().get(2);
        assertEquals(60, shown.length());
        assertTrue(shown.endsWith("... (2050)"));
        assertEquals(SHADOWS_ID, state.getBookIds().get(2));   // the id is never shortened
    }

    @Test
    void searchWithNoResults_showsErrorAndEmptyList() {
        dao = new FakeDao(new ArrayList<>());
        controller = new LookUpBookController(new LookUpBookInteractor(dao,
                new LookUpBookPresenter(searchViewModel, readerViewModel, navViewModel)));

        controller.execute("Emma");

        assertTrue(searchViewModel.getState().getResults().isEmpty());
        assertEquals("No books found", searchViewModel.getState().getError());
    }

    @Test
    void emptySearch_showsErrorWithoutSearching() {
        controller.execute("     ");

        assertEquals("Please enter a book title", searchViewModel.getState().getError());
        assertEquals(0, dao.searches);
    }

    @Test
    void cantConnect_showsErrorAndEmptyList() {
        dao.shouldThrow = true;

        controller.execute("pride");

        assertTrue(searchViewModel.getState().getResults().isEmpty());
        assertEquals("Could not reach the library, please try again", searchViewModel.getState().getError());
    }

    @Test
    void searchThenOpenBook_showsTextInReaderAndSwitchesScreen() {
        controller.execute("pride");

        controller.openBook(PRIDE_ID);

        assertEquals("Pride and Prejudice", readerViewModel.getState().getTitle());
        assertEquals(PRIDE_TEXT, readerViewModel.getState().getText());
        assertEquals(ViewNames.READER_VIEW, navViewModel.getState());
        assertEquals("", searchViewModel.getState().getError());
        assertEquals(3, searchViewModel.getState().getResults().size());   // kept for when the user goes back
    }

    @Test
    void openBookCantConnect_staysOnSearchWithErrorAndKeepsResults() {
        controller.execute("pride");
        dao.shouldThrow = true;

        controller.openBook(PRIDE_ID);

        assertNotEquals(ViewNames.READER_VIEW, navViewModel.getState());
        assertEquals("Could not load this book's text, please try again", searchViewModel.getState().getError());
        assertEquals(3, searchViewModel.getState().getResults().size());
    }

    @Test
    void openBookWithNoText_staysOnSearchWithError() {
        controller.execute("power");

        controller.openBook(POWER_ID);

        assertNotEquals(ViewNames.READER_VIEW, navViewModel.getState());
        assertEquals("No text is available for this book", searchViewModel.getState().getError());
    }

    @Test
    void openBookWithNoSelection_showsSelectFirstError() {
        controller.execute("pride");

        controller.openBook("");

        assertNotEquals(ViewNames.READER_VIEW, navViewModel.getState());
        assertEquals("Select a book first", searchViewModel.getState().getError());
    }

    @Test
    void openThenGoBack_realScreensSwitchCards() throws Exception {
        LookUpBookView searchView = new LookUpBookView(controller, searchViewModel);
        ReaderView readerView = new ReaderView(readerViewModel, navViewModel);
        ViewManager viewManager = new ViewManager(navViewModel);
        viewManager.add(searchView, ViewNames.LOOKUP_VIEW);
        viewManager.add(readerView, ViewNames.READER_VIEW);

        controller.execute("pride");
        controller.openBook(PRIDE_ID);
        waitForSwing();

        assertTrue(readerView.isVisible());
        assertFalse(searchView.isVisible());

        navViewModel.setState(ViewNames.LOOKUP_VIEW);   // what the "Back to search" button does
        waitForSwing();

        assertTrue(searchView.isVisible());
        assertFalse(readerView.isVisible());
    }
}
