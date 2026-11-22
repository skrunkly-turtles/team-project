package interface_adapter;

/** Tracks which Swing screen should currently be visible. */
public final class ViewManagerModel extends ViewModel<String> {
    /** Begins with the login screen. */
    public ViewManagerModel() { super("login"); }

    /** Requests a named view. */
    public void show(String viewName) { setState(viewName); }
}
