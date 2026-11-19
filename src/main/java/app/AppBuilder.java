package app;
import data_access.FileUserDataAccessObject;
import data_access.InMemoryCurrentUserSession;
import data_access.Pbkdf2PasswordHasher;
import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordViewModel;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import java.awt.BorderLayout;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import models.CommonUserFactory;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInteractor;
import use_case.signup.SignupInteractor;
import view.LoginView;
import view.SignupView;
import view.ViewManager;
/** Interim composition root with working account login and logout. */
public final class AppBuilder {
  /** Builds the login/logout demonstrator; later PRs insert a general profile. */
  public JFrame build() {
    FileUserDataAccessObject accounts = new FileUserDataAccessObject(Path.of("data", "accounts.properties"));
    InMemoryCurrentUserSession session = new InMemoryCurrentUserSession();
    ViewManagerModel nav = new ViewManagerModel();
    SignupViewModel signModel = new SignupViewModel();
    LoginViewModel logModel = new LoginViewModel();
    HomeViewModel homeModel = new HomeViewModel();
    SignupController sign = new SignupController(new SignupInteractor(accounts,
        new SignupPresenter(signModel, logModel, nav), new CommonUserFactory(),
        new Pbkdf2PasswordHasher()), nav);
    LoginController login = new LoginController(new LoginInteractor(accounts,
        new LoginPresenter(logModel, homeModel, nav), new Pbkdf2PasswordHasher(), session), nav);
    LogoutController logout = new LogoutController(new LogoutInteractor(session,
        new LogoutPresenter(logModel, homeModel, new ProfileViewModel(),
            new ChangePasswordViewModel(), nav)));
    ViewManager views = new ViewManager(nav);
    views.add(new LoginView(login, logModel), "login");
    views.add(new SignupView(sign, signModel), "signup");
    JPanel home = new JPanel(new BorderLayout());
    JLabel heading = new JLabel("Signed in", JLabel.CENTER);
    home.add(heading, BorderLayout.CENTER);
    JButton logoutButton = new JButton("Log Out");
    logoutButton.addActionListener(e -> logout.execute());
    home.add(logoutButton, BorderLayout.SOUTH);
    homeModel.addPropertyChangeListener(e -> heading.setText("Welcome, " + ((HomeState) e.getNewValue()).getUsername()));
    views.add(home, "home");
    JFrame frame = new JFrame("Passage Map | Account Login and Logout");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(views);
    frame.setSize(740, 480);
    return frame;
  }
}
