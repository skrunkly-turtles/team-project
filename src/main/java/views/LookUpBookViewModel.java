package views;

public class LookUpBookViewModel {
    private LookUpBookState state;

    public void setState(LookUpBookState newState) {
//        state.setResults(null);
        state = newState;
    }

    public LookUpBookState getState() {
//        return getState();
        return state;
    }

    public void addListener(LookUpBookViewModelListener listener) {
    }

    public void notifyStateChanged() {
    }
}
