package views;

import java.util.ArrayList;
import java.util.List;

public class LookUpBookViewModel {
    private LookUpBookState state = new LookUpBookState(new ArrayList<>(), "");
    private final List<LookUpBookViewModelListener> listeners = new ArrayList<>();

    public void setState(LookUpBookState newState) {
//        state.setResults(null);
        state = newState;
        notifyStateChanged();
    }

    public LookUpBookState getState() {
        return state;
    }

    public void addListener(LookUpBookViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (LookUpBookViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }
}
