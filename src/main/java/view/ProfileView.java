package view;

import interface_adapter.logout.LogoutController;
import interface_adapter.profile.ProfileController;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.add_friend.AddFriendController;
import interface_adapter.remove_friend.RemoveFriendController;
import interface_adapter.respond_friend.RespondFriendController;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

/**
 * General account profile page owned by the account developer.
 *
 * <p>Friend- and pin-management widgets are provided as Swing components by the
 * corresponding feature owners. This view does not implement their use cases.
 */
public final class ProfileView extends JPanel implements PropertyChangeListener {
  private final ProfileController controller;
  private final LogoutController logoutController;
  private final JLabel usernameLabel = new JLabel("", SwingConstants.CENTER);
  private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
  private final JPanel friendsSlot = new JPanel(new BorderLayout());
  private final JPanel pinsSlot = new JPanel(new BorderLayout());
  private final AddFriendController addFriendController;
  private final RespondFriendController respondController;
  private final DefaultListModel<String> friendsModel = new DefaultListModel<>();
  private final JList<String> friendsList = new JList<>(friendsModel);
  private final DefaultListModel<String> requestsModel = new DefaultListModel<>();
  private final JList<String> requestsList = new JList<>(requestsModel);
  private final JCheckBox confirmLogout = new JCheckBox("Ask before logging out", true);
  private final RemoveFriendController removeFriendController;
  private final JList<String> friendsList = new JList<>(friendsModel);

  /**
   * Creates the account-owned profile shell with independently replaceable feature slots.
   *
   * @param controller controller for profile-related navigation
   * @param logoutController controller responsible for ending the active session
   * @param profileViewModel observable non-sensitive account display state
   */
  public ProfileView(ProfileController controller, LogoutController logoutController,
      ProfileViewModel profileViewModel, AddFriendController addFriendController, RemoveFriendController removeFriendController, RespondFriendController respondController) {
    this.controller = controller;
    this.logoutController = logoutController;
    this.addFriendController = addFriendController;
    this.removeFriendController = removeFriendController;
    this.respondController = respondController;
    profileViewModel.addPropertyChangeListener(this);
    setLayout(new BorderLayout(16, 16));
    setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
    add(makeHeader(), BorderLayout.NORTH);
    add(makeContent(), BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(780, 490));
  }

  /** Builds the upper row with account name and the account-actions dropdown. */
  private JPanel makeHeader() {
    JPanel header = new JPanel(new BorderLayout(12, 12));
    JLabel heading = new JLabel("My Profile");
    heading.setFont(heading.getFont().deriveFont(22f));
    header.add(heading, BorderLayout.WEST);
    JButton back = new JButton("Back to map");
    back.addActionListener(event -> controller.showHome());
    JButton accountMenu = new JButton("Account \u25be");
    JPopupMenu menu = new JPopupMenu();
    JMenuItem changePassword = new JMenuItem("Change Password");
    JMenuItem settings = new JMenuItem("Settings");
    JMenuItem logout = new JMenuItem("Log Out");
    changePassword.addActionListener(event -> controller.showChangePassword());
    settings.addActionListener(event -> openSettings());
    logout.addActionListener(event -> requestLogout());
    menu.add(changePassword);
    menu.add(settings);
    menu.addSeparator();
    menu.add(logout);
    accountMenu.addActionListener(event -> menu.show(accountMenu, 0, accountMenu.getHeight()));
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.add(back);
    actions.add(accountMenu);
    header.add(actions, BorderLayout.EAST);
    return header;
  }

  /** Builds identity display and two injection points for other members' features. */
  private JPanel makeContent() {
    JPanel content = new JPanel(new BorderLayout(14, 14));
    JPanel identity = new JPanel(new GridLayout(2, 1, 0, 5));
    usernameLabel.setFont(usernameLabel.getFont().deriveFont(18f));
    identity.add(usernameLabel);
    identity.add(new JLabel("Your passage-map account", SwingConstants.CENTER));
    identity.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(14, 10, 14, 10)));
    content.add(identity, BorderLayout.NORTH);

    JPanel features = new JPanel(new GridLayout(1, 2, 16, 0));
    friendsSlot.setBorder(BorderFactory.createTitledBorder("Friends"));
    pinsSlot.setBorder(BorderFactory.createTitledBorder("My Pins"));
    setFriendsComponent(makeFriendsPanel());;
    setPinsComponent(new JLabel("Katelyn's saved-pins component goes here",
        SwingConstants.CENTER));
    features.add(friendsSlot);
    features.add(pinsSlot);
    content.add(features, BorderLayout.CENTER);
    return content;
  }

  /** Builds the friends list with an add button, and the incoming requests below it. */
  private JPanel makeFriendsPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    JButton add = new JButton("Add friend");
    JButton remove = new JButton("Remove");
    add.addActionListener(event -> openAddFriend());
    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonRow.add(add);
    remove.addActionListener(event -> openRemoveFriend());
    buttonRow.add(remove);
    panel.add(buttonRow, BorderLayout.NORTH);
    panel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

    JPanel lists = new JPanel(new GridLayout(2, 1, 0, 8));
    lists.add(new JScrollPane(friendsList));
    lists.add(makeRequestsPanel());
    panel.add(lists, BorderLayout.CENTER);
    return panel;
  }

  /** Builds the incoming-requests list with Accept and Decline buttons. */
  private JPanel makeRequestsPanel() {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel.setBorder(BorderFactory.createTitledBorder("Friend requests"));
    JButton accept = new JButton("Accept");
    JButton decline = new JButton("Decline");
    accept.addActionListener(event -> {
      String selected = requestsList.getSelectedValue();
      if (selected != null) {
        respondController.accept(selected);
      }
    });
    decline.addActionListener(event -> {
      String selected = requestsList.getSelectedValue();
      if (selected != null) {
        respondController.decline(selected);
      }
    });
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttons.add(accept);
    buttons.add(decline);
    panel.add(new JScrollPane(requestsList), BorderLayout.CENTER);
    panel.add(buttons, BorderLayout.SOUTH);
    return panel;
  }

  /** Asks for a username in a modal dialog and delegates to the controller. */
  private void openAddFriend() {
    String name = JOptionPane.showInputDialog(this, "Friend's username:", "Add friend",
            JOptionPane.PLAIN_MESSAGE);
    if (name != null) {
      addFriendController.execute(name.trim());
    }
  }

  /** Confirms in a modal dialog, then removes the selected friend. */
  private void openRemoveFriend() {
    String selected = friendsList.getSelectedValue();
    if (selected == null) {
      return;
    }
    int answer = JOptionPane.showConfirmDialog(this, "Remove " + selected + " from your friends?",
            "Remove friend", JOptionPane.YES_NO_OPTION);
    if (answer == JOptionPane.YES_OPTION) {
      removeFriendController.execute(selected);
    }
  }

  /**
   * Adds a real friends widget when Katelyn's implementation becomes available.
   *
   * @param component reusable friend-management Swing component
   */
  public void setFriendsComponent(JComponent component) {
    replaceSlot(friendsSlot, component);
  }

  /**
   * Adds a real pins widget when Katelyn's implementation becomes available.
   *
   * @param component reusable saved-pin Swing component
   */
  public void setPinsComponent(JComponent component) {
    replaceSlot(pinsSlot, component);
  }

  /** Replaces a placeholder with the provided feature component. */
  private void replaceSlot(JPanel slot, JComponent component) {
    slot.removeAll();
    slot.add(component, BorderLayout.CENTER);
    slot.revalidate();
    slot.repaint();
  }

  /** Opens a functional session-level preference for logout confirmation. */
  private void openSettings() {
    JOptionPane.showMessageDialog(this, confirmLogout,
        "Account Settings (this session)", JOptionPane.PLAIN_MESSAGE);
  }

  /** Confirms if configured, then delegates the logout operation to its controller. */
  private void requestLogout() {
    if (confirmLogout.isSelected()) {
      int answer = JOptionPane.showConfirmDialog(this, "Log out of your account?",
          "Confirm logout", JOptionPane.YES_NO_OPTION);
      if (answer != JOptionPane.YES_OPTION) {
        return;
      }
    }
    logoutController.execute();
  }

  /** Applies the latest profile state without ever displaying a password hash. */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    ProfileState state = (ProfileState) event.getNewValue();
    usernameLabel.setText(state.getUsername() == null ? "" : "@" + state.getUsername());
    statusLabel.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
    friendsModel.clear();
    for (String friend : state.getFriends()) {
      friendsModel.addElement(friend);
    }
    requestsModel.clear();
    for (String request : state.getFriendRequests()) {
      requestsModel.addElement(request);
    }
  }
}
