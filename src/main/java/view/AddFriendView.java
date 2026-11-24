package view;

import interface_adapter.add_friend.AddFriendController;
import interface_adapter.add_friend.AddFriendState;
import interface_adapter.add_friend.AddFriendViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Form for adding a friend by username, shown inside FriendsView's modal dialog. */
public final class AddFriendView extends JPanel implements PropertyChangeListener {
    private final AddFriendController controller;
    private final JTextField usernameField = new JTextField(14);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    /**
     * Creates the form.
     *
     * @param controller controller that submits add-friend requests
     * @param viewModel observable display state for this form
     */
    public AddFriendView(AddFriendController controller, AddFriendViewModel viewModel) {
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton addButton = new JButton("Add friend");
        addButton.addActionListener(event -> submit());
        usernameField.addActionListener(event -> submit());

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputRow.add(new JLabel("Username:"));
        inputRow.add(usernameField);
        inputRow.add(addButton);

        add(inputRow, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.CENTER);
    }

    /** Sends the typed username to the controller. */
    private void submit() {
        controller.execute(usernameField.getText().trim());
    }

    /** Shows the latest outcome and clears the field after a successful add. */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        AddFriendState state = (AddFriendState) event.getNewValue();
        statusLabel.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
        statusLabel.setForeground(state.isSuccess() ? new Color(0, 128, 0) : Color.RED);
        if (state.isSuccess()) {
            usernameField.setText("");
        }
    }
}