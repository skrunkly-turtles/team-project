package data_access;

import java.util.List;
import models.Pin;
import models.User;
import use_case.add_friend.AddFriendUserDataAccessInterface;
import use_case.change_password.ChangePasswordUserDataAccessInterface;
import use_case.login.LoginUserDataAccessInterface;
import use_case.profile.ProfileUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;

/** One type shared by the local and online compositions. */
public interface UserStore extends SignupUserDataAccessInterface,
    LoginUserDataAccessInterface, ChangePasswordUserDataAccessInterface,
    ProfileUserDataAccessInterface, AddFriendUserDataAccessInterface {
  List<User> getAll();
  void updatePins(String username, List<Pin> pins);
  void savePin(String username, Pin pin);
  void updatePin(String username, Pin pin);
  void deletePin(String username, int pinId);
  void updateVisibility(String username, boolean visible);
  void acceptFriendRequest(String username, String requesterUsername);
  void declineFriendRequest(String username, String requesterUsername);
  void removeFriend(String username, String friendUsername);
}
