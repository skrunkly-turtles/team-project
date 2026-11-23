package presenter;

import outputBoundary.SelectPassageOutputBoundary;
import outputBoundary.SelectPassageOutputData;
import views.ReaderState;
import views.ViewModel;

public class SelectPassagePresenter implements SelectPassageOutputBoundary {
    private final ViewModel<ReaderState> readerViewModel;

    public SelectPassagePresenter(ViewModel<ReaderState> readerViewModel) {
        this.readerViewModel = readerViewModel;
    }

    @Override
    public void prepareSuccessView(SelectPassageOutputData outputData) {
        readerViewModel.setState(readerViewModel.getState()
                .withMessage("Passage saved (" + outputData.getWordCount() + " words)"));
    }

    @Override
    public void prepareFailView(String error) {
        readerViewModel.setState(readerViewModel.getState().withMessage(error));
    }
}
