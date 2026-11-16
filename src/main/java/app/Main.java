package app;

import controller.LookUpBookController;
import dataAccess.LibraryApiDataAccessObject;
import presenter.LookUpBookPresenter;
import userCase.LookUpBookInteractor;
import views.LookUpBookView;
import views.LookUpBookViewModel;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Built from the outside in, so no object needs something that doesn't exist yet.
        LookUpBookViewModel viewModel = new LookUpBookViewModel();
        LookUpBookPresenter presenter = new LookUpBookPresenter(viewModel);
        LibraryApiDataAccessObject dataAccess = new LibraryApiDataAccessObject();
        LookUpBookInteractor interactor = new LookUpBookInteractor(dataAccess, presenter);
        LookUpBookController controller = new LookUpBookController(interactor);
        LookUpBookView view = new LookUpBookView(controller, viewModel);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Library");
            frame.setContentPane(view);
            frame.setSize(500, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
