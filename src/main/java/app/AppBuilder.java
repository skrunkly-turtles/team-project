package app;

import data_access.JsonUserDataAccessObject;
import dataAccess.LibraryApiDataAccessObject;
import userCase.LookUpBookDataAccessInterface;
import java.util.function.Supplier;
import view.BookSelectionPanel;
import data_access.InMemoryCurrentUserSession;
import data_access.Pbkdf2PasswordHasher;
import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.ChangePasswordPresenter;
import interface_adapter.change_password.ChangePasswordViewModel;
import interface_adapter.home.HomeViewModel;
import interface_adapter.map.MapController;
import interface_adapter.map.LocationSelectionController;
import interface_adapter.map.LocationSelectionPresenter;
import interface_adapter.map.LocationSelectionViewModel;
import use_case.select_location.SelectLocationInteractor;
import use_case.select_location.SelectedLocation;
import java.util.function.Consumer;
import interface_adapter.map.MapPresenter;
import interface_adapter.map.MapViewModel;
import use_case.display_map.DisplayMapInteractor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import view.MapPanel;
import view.PinDetailsPanel;
import interface_adapter.map.PinDetailsController;
import interface_adapter.map.PinDetailsPresenter;
import interface_adapter.map.PinDetailsViewModel;
import use_case.pin_details.PinDetailsInteractor;
import use_case.pin_details.PinDetailsOutputData;
import view.MapboxTileFactoryInfo;
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
import interface_adapter.add_friend.AddFriendController;
import interface_adapter.add_friend.AddFriendPresenter;
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
import use_case.add_friend.AddFriendInteractor;
import view.ChangePasswordView;
import view.HomeView;
import view.HomeSidebar;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import use_case.select_location.SelectLocationOutputData.Stage;
import view.LoginView;
import view.ProfileView;
import view.SignupView;
import view.ViewManager;

/**
 * Composition root: wires use cases to their concrete adapters and Swing screens.
 *
 * <p>Assembles the shared account and map flows. Independent profile pin/friend
 * components remain injectable.
 */
public final class AppBuilder {
  private Path accountFile = Path.of("data", "users.json");
  private JComponent friendsComponent;
  private JComponent pinsComponent;
  private String mapboxToken = System.getenv("MAPBOX_ACCESS_TOKEN");
  private Consumer<SelectedLocation> onLocationSelected;
  private Runnable onFilterPins;
  private Supplier<LookUpBookDataAccessInterface> libraryFactory = LibraryApiDataAccessObject::new;

  /** Supplies the library provider, allowing offline integration checks.
   * @param factory creates an independent library session
   * @return this builder
   */
  public AppBuilder libraryFactory(Supplier<LookUpBookDataAccessInterface> factory) {
    libraryFactory = Objects.requireNonNull(factory, "factory");
    return this;
  }

  /**
   * Connects the shared filtering action to the sidebar.
   * @param action callback supplied by the filtering feature
   * @return this builder
   */
  public AppBuilder onFilterPins(Runnable action) {
    onFilterPins = Objects.requireNonNull(action, "action");
    return this;
  }

  /**
   * Connects confirmed coordinates to the next creation step.
   * @param action passage-navigation callback; no pin is saved here
   * @return this builder
   */
  public AppBuilder onLocationSelected(Consumer<SelectedLocation> action) {
    onLocationSelected = Objects.requireNonNull(action, "action");
    return this;
  }

  /**
   * Configures map imagery; null displays local setup instructions.
   * @param token public Mapbox token, or null
   * @return this builder
   */
  public AppBuilder mapboxToken(String token) {
    mapboxToken = token;
    return this;
  }

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

  /**
   * Assembles the account screens and map without creating a native window.
   * @return the assembled navigation component
   */
  public ViewManager buildContent() {
    JsonUserDataAccessObject accounts = new JsonUserDataAccessObject(accountFile);
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
    AddFriendPresenter addFriendPresenter = new AddFriendPresenter(profileModel);
    AddFriendController addFriendController = new AddFriendController(
            new AddFriendInteractor(accounts, addFriendPresenter, session));

    ProfileView profileView = new ProfileView(profileController, logoutController, profileModel, addFriendController);
    if (friendsComponent != null) {
      profileView.setFriendsComponent(friendsComponent);
    }
    if (pinsComponent != null) {
      profileView.setPinsComponent(pinsComponent);
    }
    ViewManager viewManager = new ViewManager(navigator);
    viewManager.add(new LoginView(loginController, loginModel), "login");
    viewManager.add(new SignupView(signupController, signupModel), "signup");
    MapViewModel mapModel = new MapViewModel();
    MapController mapController = new MapController(
        new DisplayMapInteractor(new MapPresenter(mapModel)));
    DefaultTileFactory tileFactory = null;
    if (mapboxToken != null && !mapboxToken.isBlank()) {
      tileFactory = new DefaultTileFactory(new MapboxTileFactoryInfo(mapboxToken));
      tileFactory.setThreadPoolSize(4);
    }
    MapPanel mapPanel = new MapPanel(tileFactory, mapModel);
    // Details/account integration created with AI assistance.
    PinDetailsViewModel detailsModel = new PinDetailsViewModel();
    PinDetailsController detailsController = new PinDetailsController(
        new PinDetailsInteractor(new PinDetailsPresenter(detailsModel)));
    PinDetailsPanel detailsPanel = new PinDetailsPanel(detailsController, detailsModel);
    LocationSelectionViewModel selectionModel = new LocationSelectionViewModel();
    BookSelectionPanel[] bookStep = new BookSelectionPanel[1];
    LocationSelectionController selectionController = new LocationSelectionController(
        new SelectLocationInteractor(new LocationSelectionPresenter(selectionModel, location -> {
          if (onLocationSelected == null) {
            bookStep[0].open(location);
            navigator.setState("pin-book");
          } else {
            onLocationSelected.accept(location);
          }
        })));
    bookStep[0] = new BookSelectionPanel(libraryFactory,
        () -> { selectionController.resume(); navigator.setState("home"); },
        () -> { selectionController.cancel(); navigator.setState("home"); });
    viewManager.add(bookStep[0], "pin-book");
    mapPanel.enableLocationSelection(selectionController, selectionModel);
    detailsModel.addPropertyChangeListener(event -> {
      if (detailsModel.getState().getStage() == PinDetailsOutputData.Stage.BACK) {
        selectionController.resume();
      } else if (detailsModel.getState().getStage() == PinDetailsOutputData.Stage.CANCELLED
          && selectionModel.getState().getStage() != Stage.INACTIVE) {
        selectionController.cancel();
      }
    });
    selectionModel.addPropertyChangeListener(event -> {
      if (selectionModel.getState().getStage() == Stage.INACTIVE) {
        detailsController.cancel();
        bookStep[0].reset();
      }
    });
    homeModel.addPropertyChangeListener(event -> {
      if (homeModel.getState().getUsername() == null) {
        selectionController.cancel();
      }
    });
    JLabel navigationStatus = new JLabel(" ");
    HomeSidebar sidebar = new HomeSidebar(selectionController::start,
        () -> {
          if (onFilterPins == null) {
            navigationStatus.setText("Filter Pins: Katelyn's filtering action will connect here.");
          } else {
            onFilterPins.run();
          }
        }, profileController::execute, logoutController::execute);
    boolean mapUnavailable = tileFactory == null;
    sidebar.setCreationActive(mapUnavailable);
    selectionModel.addPropertyChangeListener(event -> sidebar.setCreationActive(
        mapUnavailable || selectionModel.getState().getStage() != Stage.INACTIVE));
    homeModel.addPropertyChangeListener(event -> {
      if (homeModel.getState().getUsername() == null) { navigationStatus.setText(" "); }
    });
    HomeView homeView = new HomeView(mapPanel, mapController, sidebar, homeModel);
    homeView.add(navigationStatus, BorderLayout.SOUTH);
    homeView.add(detailsPanel, BorderLayout.EAST);
    viewManager.add(homeView, "home");
    viewManager.add(profileView, "profile");
    viewManager.add(new ChangePasswordView(changeController, changeModel),
        "change-password");

    viewManager.setPreferredSize(new Dimension(1000, 700));
    return viewManager;
  }

  /**
   * Builds the desktop window containing the account and map flows.
   * @return configured application window
   */
  public JFrame build() {
    JFrame frame = new JFrame("Passage Map | Accounts and Profile");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(buildContent());
    frame.setMinimumSize(new Dimension(780, 490));
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }
}
