package presenter;

import outputBoundary.SearchInBookOutputBoundary;
import outputBoundary.SearchInBookOutputData;
import views.ReaderState;
import views.ViewModel;

public class SearchInBookPresenter implements SearchInBookOutputBoundary {
    private final ViewModel<ReaderState> readerViewModel;

    public SearchInBookPresenter(ViewModel<ReaderState> readerViewModel) {
        this.readerViewModel = readerViewModel;
    }

    @Override
    public void prepareSuccessView(SearchInBookOutputData outputData) {
        readerViewModel.setState(readerViewModel.getState().withSearchResults(
                outputData.getMatchStarts(), outputData.getMatchLength(), outputData.getSnippets()));
    }

    @Override
    public void prepareFailView(String error) {
        readerViewModel.setState(readerViewModel.getState().withNoMatches(error));
    }
}
