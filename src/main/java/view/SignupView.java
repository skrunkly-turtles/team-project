package view;

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
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupViewModel;

/** Java Swing registration screen with username, password and confirmation fields. */
public final class SignupView extends JPanel implements PropertyChangeListener {
    private final SignupController controller;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JLabel status = new JLabel(" ");

    /** Builds and registers the signup screen as an observer. */
    public SignupView(SignupController controller, SignupViewModel viewModel) {
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
        JLabel heading = new JLabel("Create account");
        heading.setHorizontalAlignment(JLabel.CENTER);
        add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 12));
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);
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

    /** Submits input and immediately clears password fields and temporary arrays. */
    private void submit() {
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmField.getPassword();
        try {
            controller.execute(usernameField.getText(), new String(password), new String(confirmation));
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            passwordField.setText("");
            confirmField.setText("");
        }
    }

    /** Displays validation errors from the presenter. */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        interface_adapter.signup.SignupState state =
                (interface_adapter.signup.SignupState) event.getNewValue();
        status.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
    }
}
