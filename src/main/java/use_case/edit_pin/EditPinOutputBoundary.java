package use_case.edit_pin;

public interface EditPinOutputBoundary {
    void prepareSuccessView(EditPinOutputData output, String message);

    void prepareFailureView(String message);
}