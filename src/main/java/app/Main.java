package app;

import controller.LookUpBookController;
import dataAccess.LibraryApiDataAccessObject;
import presenter.LookUpBookPresenter;
import userCase.LookUpBookInteractor;
import views.LookUpBookState;
import views.LookUpBookView;
import views.ReaderState;
import views.ReaderView;
import views.ViewManager;
import views.ViewModel;
import views.ViewNames;

import javax.swing.*;
import java.awt.CardLayout;
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
        ReaderView readerView = new ReaderView(readerViewModel, viewManagerModel);

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
            frame.setVisible(true);
        });
    }
}
