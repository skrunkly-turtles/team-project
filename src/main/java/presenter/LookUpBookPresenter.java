package presenter;

import outputBoundary.LookUpBookOutputBoundary;
import outputBoundary.LookUpBookOutputData;
import outputBoundary.LookUpBookReaderData;
import views.LookUpBookState;
import views.ReaderState;
import views.ViewModel;
import views.ViewNames;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookPresenter implements LookUpBookOutputBoundary {
    private static final int MAX_LENGTH = 60;
    private final ViewModel<LookUpBookState> viewModel;
    private final ViewModel<ReaderState> readerViewModel;     // null if only searching is wired up
    private final ViewModel<String> viewManagerModel;         // null if only searching is wired up

    public LookUpBookPresenter(ViewModel<LookUpBookState> viewModel) {
        this(viewModel, null, null);
    }

    // Optional constructor: also lets the presenter show the reader screen.
    public LookUpBookPresenter(ViewModel<LookUpBookState> viewModel, ViewModel<ReaderState> readerViewModel,
                               ViewModel<String> viewManagerModel) {
        this.viewModel = viewModel;
        this.readerViewModel = readerViewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(LookUpBookOutputData outputData) {
        List<String> display = new ArrayList<>();
        for (String book : outputData.getBooks()) {
            display.add(truncate(book));
        }
        viewModel.setState(new LookUpBookState(display, outputData.getBookIds(), ""));
        System.out.println("Successfully loaded Books");
        System.out.println(outputData.getBooks());
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.setState(new LookUpBookState(new ArrayList<>(), error));
    }

    @Override
    public void prepareReaderView(LookUpBookReaderData readerData) {
        if (readerViewModel == null || viewManagerModel == null) {
            return;   // the reader screen is not wired up
        }
        clearError();   // removes the "Opening..." message from the search screen
        readerViewModel.setState(new ReaderState(readerData.getBookId(), readerData.getTitle(), readerData.getText()));
        viewManagerModel.setState(ViewNames.READER_VIEW);
    }

    @Override
    public void prepareOpenFailView(String error) {
        LookUpBookState current = viewModel.getState();
        viewModel.setState(new LookUpBookState(current.getResults(), current.getBookIds(), error));
    }

    private void clearError() {
        LookUpBookState current = viewModel.getState();
        viewModel.setState(new LookUpBookState(current.getResults(), current.getBookIds(), ""));
    }

    private String truncate(String text) {
        if (text.length() <= MAX_LENGTH) {
            return text;
        }
        int yearStart = text.lastIndexOf(" (");
        if (yearStart == -1) {
            return text.substring(0, MAX_LENGTH - 3) + "...";
        }
        String year = text.substring(yearStart);
        int keep = MAX_LENGTH - year.length() - 3;
        return text.substring(0, keep) + "..." + year;
    }
}
