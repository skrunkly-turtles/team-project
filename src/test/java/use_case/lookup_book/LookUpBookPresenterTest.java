package use_case.lookup_book;

import interface_adapter.lookup_book.LookUpBookPresenter;
import models.Book;
import org.junit.jupiter.api.Test;
import interface_adapter.lookup_book.LookUpBookState;
import interface_adapter.lookup_book.ReaderState;
import interface_adapter.lookup_book.LookupBookViewModel;
import interface_adapter.ViewManagerModel;
import models.ViewNames;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LookUpBookPresenterTest {
    private static final String PRIDE_ID = "prideprejudice00aust";
    private static final String PRIDE_TEXT = "\"I must think your language too strong in speaking of both,\" replied Jane; \"and I hope you will be convinced of it by seeing them happy together. But enough of this. You alluded to something else. ";

    private final LookupBookViewModel<LookUpBookState> searchViewModel =
            new LookupBookViewModel<>(new LookUpBookState(new ArrayList<>(), ""));
    private final LookupBookViewModel<ReaderState> readerViewModel = new LookupBookViewModel<>(new ReaderState());
    private final ViewManagerModel navViewModel = new ViewManagerModel();

    private LookUpBookPresenter fullyWiredPresenter() {
        return new LookUpBookPresenter(searchViewModel, readerViewModel, navViewModel);
    }

    private static LookUpBookOutputData prideResult() {
        return new LookUpBookOutputData(
                List.of(new Book("Pride and Prejudice", "Jane Austen", 1813, PRIDE_ID).toString()),
                List.of(PRIDE_ID));
    }

    @Test
    void successView_setsResultsAndClearsError() {
        fullyWiredPresenter().prepareSuccessView(prideResult());

        LookUpBookState state = searchViewModel.getState();
        assertEquals(List.of("Pride and Prejudice by Jane Austen (1813)"), state.getResults());
        assertEquals(List.of(PRIDE_ID), state.getBookIds());
        assertEquals("", state.getError());
    }

    @Test
    void successViewWithShortEntry_isNotTruncated() {
        fullyWiredPresenter().prepareSuccessView(prideResult());

        assertFalse(searchViewModel.getState().getResults().get(0).contains("..."));
    }

    @Test
    void successViewWithLongEntry_isTruncatedButKeepsYear() {
        String longEntry = new Book("Preventing “Stupid” Deaths: A Comparative Analysis of Policy Failures in Pandemic Response",
                "Joy Fitzgibbon", 2050, "fitzgibbon#300pandemics").toString();
        assertTrue(longEntry.length() > 60);

        fullyWiredPresenter().prepareSuccessView(
                new LookUpBookOutputData(List.of(longEntry), List.of("fitzgibbon#300pandemics")));

        String shown = searchViewModel.getState().getResults().get(0);
        assertEquals(60, shown.length());
        assertTrue(shown.startsWith("Preventing “Stupid” Deaths"));
        assertTrue(shown.endsWith("... (2050)"));
    }

    @Test
    void failView_clearsResultsAndSetsError() {
        LookUpBookPresenter presenter = fullyWiredPresenter();
        presenter.prepareSuccessView(prideResult());

        presenter.prepareFailView("No books found");

        assertTrue(searchViewModel.getState().getResults().isEmpty());
        assertEquals("No books found", searchViewModel.getState().getError());
    }

    @Test
    void readerView_setsReaderStateAndSwitchesScreen() {
        fullyWiredPresenter().prepareReaderView(new LookUpBookReaderData("Pride and Prejudice", PRIDE_TEXT));

        assertEquals("Pride and Prejudice", readerViewModel.getState().getTitle());
        assertEquals(PRIDE_TEXT, readerViewModel.getState().getText());
        assertEquals(ViewNames.READER_VIEW, navViewModel.getState());
    }

    @Test
    void readerView_clearsOpeningMessageButKeepsSearchResults() {
        LookUpBookPresenter presenter = fullyWiredPresenter();
        presenter.prepareSuccessView(prideResult());
        searchViewModel.setState(new LookUpBookState(
                searchViewModel.getState().getResults(), searchViewModel.getState().getBookIds(), "Opening book..."));

        presenter.prepareReaderView(new LookUpBookReaderData("Pride and Prejudice", PRIDE_TEXT));

        assertEquals("", searchViewModel.getState().getError());
        assertEquals(1, searchViewModel.getState().getResults().size());
    }

    @Test
    void readerViewNotWired_isIgnoredWithoutError() {
        navViewModel.setState(ViewNames.LOOKUP_VIEW);
        LookUpBookPresenter searchOnly = new LookUpBookPresenter(searchViewModel);

        assertDoesNotThrow(() ->
                searchOnly.prepareReaderView(new LookUpBookReaderData("Pride and Prejudice", PRIDE_TEXT)));

        assertEquals(ViewNames.LOOKUP_VIEW, navViewModel.getState());
        assertEquals("", readerViewModel.getState().getTitle());
    }

    @Test
    void openFailView_keepsResultsAndSetsError() {
        LookUpBookPresenter presenter = fullyWiredPresenter();
        navViewModel.setState(ViewNames.LOOKUP_VIEW);
        searchViewModel.setState(new LookUpBookState(
                new ArrayList<>(List.of("Dune")), new ArrayList<>(List.of("id1")), ""));

        presenter.prepareOpenFailView("Could not open book.");

        LookUpBookState state = searchViewModel.getState();
        assertEquals(List.of("Dune"), state.getResults());
        assertEquals(List.of("id1"), state.getBookIds());
        assertEquals("Could not open book.", state.getError());
        assertEquals(ViewNames.LOOKUP_VIEW, navViewModel.getState());
    }
}
