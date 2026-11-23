package app;

import controller.LookUpBookController;
import controller.SearchInBookController;
import controller.SelectPassageController;
import dataAccess.InMemoryPassageDataAccessObject;
import dataAccess.LibraryApiDataAccessObject;
import presenter.LookUpBookPresenter;
import presenter.SearchInBookPresenter;
import presenter.SelectPassagePresenter;
import userCase.LookUpBookInteractor;
import userCase.SearchInBookInteractor;
import userCase.SelectPassageInteractor;
import views.LookUpBookState;
import views.LookUpBookView;
import views.ReaderState;
import views.ReaderView;
import views.ViewManager;
import views.ViewModel;
import views.ViewNames;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ViewModel<LookUpBookState> viewModel = new ViewModel<>(new LookUpBookState(new ArrayList<>(), ""));
        ViewModel<ReaderState> readerViewModel = new ViewModel<>(new ReaderState());
        ViewModel<String> viewManagerModel = new ViewModel<>(ViewNames.LOOKUP_VIEW);
        LookUpBookPresenter presenter = new LookUpBookPresenter(viewModel, readerViewModel, viewManagerModel);
        LibraryApiDataAccessObject dataAccess = new LibraryApiDataAccessObject();
        LookUpBookInteractor interactor = new LookUpBookInteractor(dataAccess, presenter);
        LookUpBookController controller = new LookUpBookController(interactor);
        LookUpBookView view = new LookUpBookView(controller, viewModel);

        // Search inside the open book. The reader already has the text, so this use case needs no data access.
        SearchInBookPresenter searchInBookPresenter = new SearchInBookPresenter(readerViewModel);
        SearchInBookInteractor searchInBookInteractor = new SearchInBookInteractor(searchInBookPresenter);
        SearchInBookController searchInBookController = new SearchInBookController(searchInBookInteractor);

        // Select a passage and keep it in memory.
        InMemoryPassageDataAccessObject passageDataAccess = new InMemoryPassageDataAccessObject(dataAccess);
        SelectPassagePresenter selectPassagePresenter = new SelectPassagePresenter(readerViewModel);
        SelectPassageInteractor selectPassageInteractor = new SelectPassageInteractor(passageDataAccess, selectPassagePresenter);
        SelectPassageController selectPassageController = new SelectPassageController(selectPassageInteractor);

        ReaderView readerView = new ReaderView(readerViewModel, viewManagerModel, searchInBookController, selectPassageController);

        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        cards.add(view, ViewNames.LOOKUP_VIEW);
        cards.add(readerView, ViewNames.READER_VIEW);
        new ViewManager(cards, cardLayout, viewManagerModel);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Library");
            frame.setContentPane(cards);
            frame.setSize(800, 650);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(450, 400));
            frame.setVisible(true);
        });
    }
}
