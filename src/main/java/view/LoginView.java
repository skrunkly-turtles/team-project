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
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;

/** Java Swing login form for previously registered users. */
public final class LoginView extends JPanel implements PropertyChangeListener {
    private final LoginController controller;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel status = new JLabel(" ");

    /** Creates the login screen and attaches UI listeners. */
    public LoginView(LoginController controller, LoginViewModel viewModel) {
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        JLabel heading = new JLabel("Passage Map — Log in");
        heading.setHorizontalAlignment(JLabel.CENTER);
        add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 12));
        fields.add(new JLabel("Username:"));
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

    /** Attempts authentication and removes the typed password from the form. */
    private void submit() {
        char[] password = passwordField.getPassword();
        try {
            controller.execute(usernameField.getText(), new String(password));
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    /** Displays authentication or signup success messages from the presenter. */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        LoginState state = (LoginState) event.getNewValue();
        status.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
    }
}
