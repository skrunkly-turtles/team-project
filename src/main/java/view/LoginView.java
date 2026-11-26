package view;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Login uses username locally and verified email in Supabase mode. */
public final class LoginView extends JPanel implements PropertyChangeListener {
  private final LoginController controller;
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JLabel status = new JLabel(" ");

  public LoginView(LoginController controller, LoginViewModel viewModel) {
    this(controller, viewModel, false);
  }

  public LoginView(LoginController controller, LoginViewModel viewModel, boolean online) {
    this.controller = controller;
    viewModel.addPropertyChangeListener(this);
    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
    JLabel heading = new JLabel("Passage Map — Log in");
    heading.setHorizontalAlignment(JLabel.CENTER);
    add(heading, BorderLayout.NORTH);

    JPanel fields = new JPanel(new GridLayout(2, 2, 10, 12));
    fields.add(new JLabel(online ? "Email:" : "Username:"));
    fields.add(usernameField);
    fields.add(new JLabel("Password:"));
    fields.add(passwordField);
    add(fields, BorderLayout.CENTER);

    JPanel footer = new JPanel(new BorderLayout());
    status.setHorizontalAlignment(JLabel.CENTER);
    footer.add(status, BorderLayout.NORTH);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton loginButton = new JButton("Log in");
    JButton signupButton = new JButton("Create account");
    loginButton.addActionListener(event -> submit());
    passwordField.addActionListener(event -> submit());
    signupButton.addActionListener(event -> controller.showSignup());
    buttons.add(loginButton);
    buttons.add(signupButton);
    footer.add(buttons, BorderLayout.SOUTH);
    add(footer, BorderLayout.SOUTH);
  }

  private void submit() {
    char[] password = passwordField.getPassword();
    try {
      controller.execute(usernameField.getText(), new String(password));
    } finally {
      Arrays.fill(password, '\0');
      passwordField.setText("");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    LoginState state = (LoginState) event.getNewValue();
    status.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
  }
}
