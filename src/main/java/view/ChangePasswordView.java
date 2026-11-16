package view;

import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.ChangePasswordState;
import interface_adapter.change_password.ChangePasswordViewModel;
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

/** A Java Swing form to change the current authenticated account's password. */
public final class ChangePasswordView extends JPanel implements PropertyChangeListener {
  private final ChangePasswordController controller;
  private final JPasswordField currentPassword = new JPasswordField(20);
  private final JPasswordField newPassword = new JPasswordField(20);
  private final JPasswordField confirmPassword = new JPasswordField(20);
  private final JLabel message = new JLabel(" ");

  /** Connects the form to its controller and observable UI state. */
  public ChangePasswordView(ChangePasswordController controller,
      ChangePasswordViewModel viewModel) {
    this.controller = controller;
    viewModel.addPropertyChangeListener(this);
    setLayout(new BorderLayout(14, 14));
    setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));
    JLabel title = new JLabel("Change Password");
    title.setHorizontalAlignment(JLabel.CENTER);
    add(title, BorderLayout.NORTH);
    JPanel inputs = new JPanel(new GridLayout(3, 2, 12, 15));
    inputs.add(new JLabel("Current password:"));
    inputs.add(currentPassword);
    inputs.add(new JLabel("New password:"));
    inputs.add(newPassword);
    inputs.add(new JLabel("Confirm new password:"));
    inputs.add(confirmPassword);
    add(inputs, BorderLayout.CENTER);
    JPanel bottom = new JPanel(new BorderLayout());
    message.setHorizontalAlignment(JLabel.CENTER);
    bottom.add(message, BorderLayout.NORTH);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton save = new JButton("Update Password");
    JButton cancel = new JButton("Cancel");
    save.addActionListener(event -> submit());
    cancel.addActionListener(event -> {
      clearPasswords();
      controller.cancel();
    });
    buttons.add(save);
    buttons.add(cancel);
    bottom.add(buttons, BorderLayout.SOUTH);
    add(bottom, BorderLayout.SOUTH);
  }

  /** Submits three credentials, then overwrites the temporary password arrays. */
  private void submit() {
    char[] oldValue = currentPassword.getPassword();
    char[] newValue = newPassword.getPassword();
    char[] repeatedValue = confirmPassword.getPassword();
    try {
      controller.execute(new String(oldValue), new String(newValue), new String(repeatedValue));
    } finally {
      Arrays.fill(oldValue, '\0');
      Arrays.fill(newValue, '\0');
      Arrays.fill(repeatedValue, '\0');
      clearPasswords();
    }
  }

  /** Clears credentials when the user submits or cancels the form. */
  private void clearPasswords() {
    currentPassword.setText("");
    newPassword.setText("");
    confirmPassword.setText("");
  }

  /** Displays the presenter's latest validation result. */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    ChangePasswordState state = (ChangePasswordState) event.getNewValue();
    message.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
  }
}
