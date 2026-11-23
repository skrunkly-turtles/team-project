package outputBoundary;

public interface SelectPassageOutputBoundary {
    void prepareSuccessView(SelectPassageOutputData outputData);

    void prepareFailView(String error);
}
