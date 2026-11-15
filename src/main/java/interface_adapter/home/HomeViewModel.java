package interface_adapter.home;

import interface_adapter.ViewModel;

/** Observable state for the temporary authenticated home screen. */
public final class HomeViewModel extends ViewModel<HomeState> {
    /** Initializes with no authenticated user. */
    public HomeViewModel() { super(new HomeState(null)); }
}
