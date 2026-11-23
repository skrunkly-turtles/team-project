package interface_adapter.profile;

import interface_adapter.ViewModel;

/** Observable profile data read by the general Swing profile page. */
public final class ProfileViewModel extends ViewModel<ProfileState> {
  /** Creates an empty profile ViewModel. */
  public ProfileViewModel() {
    super(new ProfileState(null, ""));
  }
}
