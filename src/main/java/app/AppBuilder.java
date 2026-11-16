package app;

import data_access.FileUserDataAccessObject;
import data_access.InMemoryCurrentUserSession;
import data_access.Pbkdf2PasswordHasher;
import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.ChangePasswordPresenter;
import interface_adapter.change_password.ChangePasswordViewModel;
import interface_adapter.home.HomeViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.profile.ProfileController;
import interface_adapter.profile.ProfilePresenter;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JFrame;
import models.CommonUserFactory;
import use_case.change_password.ChangePasswordInteractor;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInteractor;
import use_case.profile.ProfileInteractor;
import use_case.signup.SignupInteractor;
import view.ChangePasswordView;
import view.HomeView;
import view.LoginView;
import view.ProfileView;
import view.SignupView;
import view.ViewManager;

/**
 * Composition root: wires use cases to their concrete adapters and Swing screens.
 *
 * <p>The map, pin, and friends code belongs to other teammates and is injected as
 * optional reusable components rather than being implemented in this application.
 */
public final class AppBuilder {
  private Path accountFile = Path.of("data", "accounts.properties");
  private JComponent friendsComponent;
  private JComponent pinsComponent;

  /**
   * Overrides the local account path, useful for integration tests or demonstrations.
   *
   * @param file file used for account persistence
   * @return this builder
   */
  public AppBuilder accountFile(Path file) {
    accountFile = Objects.requireNonNull(file, "file");
    return this;
  }

  /**
   * Accepts Katelyn's independent friends widget without depending on its classes.
   *
   * @param component friend-management panel supplied by another team member
   * @return this builder
   */
  public AppBuilder friendsComponent(JComponent component) {
    friendsComponent = component;
    return this;
  }

  /**
   * Accepts Katelyn's independent pin widget without depending on its classes.
   *
   * @param component saved-pins panel supplied by another team member
   * @return this builder
   */
  public AppBuilder pinsComponent(JComponent component) {
    pinsComponent = component;
    return this;
  }

  /** Builds a runnable account and general-profile application. */
  public JFrame build() {
    FileUserDataAccessObject accounts = new FileUserDataAccessObject(accountFile);
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
    ViewManagerModel navigator = new ViewManagerModel();
    SignupViewModel signupModel = new SignupViewModel();
    LoginViewModel loginModel = new LoginViewModel();
    HomeViewModel homeModel = new HomeViewModel();
    ProfileViewModel profileModel = new ProfileViewModel();
    ChangePasswordViewModel changeModel = new ChangePasswordViewModel();

    SignupPresenter signupPresenter = new SignupPresenter(signupModel, loginModel, navigator);
    LoginPresenter loginPresenter = new LoginPresenter(loginModel, homeModel, navigator);
    ProfilePresenter profilePresenter = new ProfilePresenter(profileModel, loginModel, navigator);
    LogoutPresenter logoutPresenter = new LogoutPresenter(loginModel, homeModel,
        profileModel, changeModel, navigator);
    ChangePasswordPresenter changePresenter = new ChangePasswordPresenter(
        changeModel, profileModel, navigator);

    SignupController signupController = new SignupController(
        new SignupInteractor(accounts, signupPresenter, new CommonUserFactory(), hasher),
        navigator);
    LoginController loginController = new LoginController(
        new LoginInteractor(accounts, loginPresenter, hasher, session), navigator);
    LogoutController logoutController = new LogoutController(
        new LogoutInteractor(session, logoutPresenter));
    ProfileController profileController = new ProfileController(
        new ProfileInteractor(session, accounts, profilePresenter), navigator);
    ChangePasswordController changeController = new ChangePasswordController(
        new ChangePasswordInteractor(accounts, changePresenter, hasher, session),
        navigator);

    ProfileView profileView = new ProfileView(profileController, logoutController, profileModel);
    if (friendsComponent != null) {
      profileView.setFriendsComponent(friendsComponent);
    }
    if (pinsComponent != null) {
      profileView.setPinsComponent(pinsComponent);
    }
    ViewManager viewManager = new ViewManager(navigator);
    viewManager.add(new LoginView(loginController, loginModel), "login");
    viewManager.add(new SignupView(signupController, signupModel), "signup");
    viewManager.add(new HomeView(logoutController, profileController, homeModel), "home");
    viewManager.add(profileView, "profile");
    viewManager.add(new ChangePasswordView(changeController, changeModel),
        "change-password");

    JFrame frame = new JFrame("Passage Map | Accounts and Profile");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(viewManager);
    frame.setMinimumSize(new Dimension(780, 490));
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }
}
