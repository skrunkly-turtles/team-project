package use_case.delete_pin;

public interface DeletePinOutputBoundary {
    void prepareSuccessView(DeletePinOutputData output, String message);

    void prepareFailureView(String message);
}