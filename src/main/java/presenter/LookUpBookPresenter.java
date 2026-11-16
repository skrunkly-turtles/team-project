package presenter;

import outputBoundary.LookUpBookOutputBoundary;
import outputBoundary.LookUpBookOutputData;
import views.LookUpBookState;
import views.LookUpBookView;
import views.LookUpBookViewModel;

import javax.swing.text.View;

public class LookUpBookPresenter implements LookUpBookOutputBoundary {
    private final LookUpBookViewModel viewModel;

    public LookUpBookPresenter(LookUpBookViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(LookUpBookOutputData outputData) {
        viewModel.setState(new LookUpBookState(outputData.getBooks(), "None"));
        System.out.println("Successfully loaded Books");
        System.out.println(outputData.getBooks());
    }

    @Override
    public void prepareFailView(String error) {
    }

    @Override
    public void prepareReaderView() {
    }
}
