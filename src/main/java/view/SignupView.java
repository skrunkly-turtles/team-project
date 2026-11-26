package view;

import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;
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

/** Registration form; optional email input is used only in Supabase mode. */
public final class SignupView extends JPanel implements PropertyChangeListener {
  private final SignupController controller;
  private final boolean online;
  private final JTextField usernameField = new JTextField(20);
  private final JTextField emailField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JPasswordField confirmField = new JPasswordField(20);
  private final JLabel status = new JLabel(" ");

  public SignupView(SignupController controller, SignupViewModel viewModel) {
    this(controller, viewModel, false);
  }

  public SignupView(SignupController controller, SignupViewModel viewModel, boolean online) {
    this.controller = controller;
    this.online = online;
    viewModel.addPropertyChangeListener(this);
    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
    JLabel heading = new JLabel("Create account");
    heading.setHorizontalAlignment(JLabel.CENTER);
    add(heading, BorderLayout.NORTH);

    JPanel fields = new JPanel(new GridLayout(online ? 4 : 3, 2, 10, 12));
    fields.add(new JLabel("Username:"));
    fields.add(usernameField);
    if (online) {
      fields.add(new JLabel("Email:"));
      fields.add(emailField);
    }
    fields.add(new JLabel("Password:"));
    fields.add(passwordField);
    fields.add(new JLabel("Confirm password:"));
    fields.add(confirmField);
    add(fields, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout(8, 8));
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton create = new JButton("Create account");
    JButton cancel = new JButton("Back to login");
    create.addActionListener(event -> submit());
    cancel.addActionListener(event -> controller.showLogin());
    buttons.add(create);
    buttons.add(cancel);
    status.setHorizontalAlignment(JLabel.CENTER);
    bottom.add(status, BorderLayout.NORTH);
    bottom.add(buttons, BorderLayout.CENTER);
    add(bottom, BorderLayout.SOUTH);
  }

  private void submit() {
    char[] password = passwordField.getPassword();
    char[] confirmation = confirmField.getPassword();
    try {
      if (online) {
        controller.execute(usernameField.getText(), emailField.getText(),
            new String(password), new String(confirmation));
      } else {
        controller.execute(usernameField.getText(), new String(password),
            new String(confirmation));
      }
    } finally {
      Arrays.fill(password, '\0');
      Arrays.fill(confirmation, '\0');
      passwordField.setText("");
      confirmField.setText("");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SignupState state = (SignupState) event.getNewValue();
    status.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
  }
}
