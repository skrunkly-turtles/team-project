package presenter;

import outputBoundary.LookUpBookOutputBoundary;
import outputBoundary.LookUpBookOutputData;

public class LookUpBookPresenter implements LookUpBookOutputBoundary {
    @Override
    public void prepareSuccessView(LookUpBookOutputData outputData) {
    }

    @Override
    public void prepareFailView(String error) {
    }

    @Override
    public void prepareReaderView() {
    }
}
