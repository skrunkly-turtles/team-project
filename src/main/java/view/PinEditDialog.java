package view;

import interface_adapter.edit_pin.EditPinController;
import interface_adapter.edit_pin.EditPinState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import models.Pin.PinData;

/** Modal form for editing one of the signed-in user's own pins. */
public final class PinEditDialog {
    private final EditPinController controller;
    private final JTextField name = new JTextField(20);
    private final JTextField latitude = new JTextField(20);
    private final JTextField longitude = new JTextField(20);
    private final JTextField bookTitle = new JTextField(20);
    private final JTextField author = new JTextField(20);
    private final JTextField year = new JTextField(20);
    private final JTextArea description = new JTextArea(3, 20);
    private final JTextArea passage = new JTextArea(5, 20);
    private final JLabel status = new JLabel(" ", SwingConstants.CENTER);
    private JDialog dialog;
    private int pinId;

    /** Creates the dialog helper. */
    public PinEditDialog(EditPinController controller) {
        this.controller = controller;
    }

    /** Opens the form prefilled with the given pin. */
    public void open(Component parent, PinData pin) {
        pinId = pin.id();
        latitude.setText(String.valueOf(pin.coordinate().getLatitude()));
        longitude.setText(String.valueOf(pin.coordinate().getLongitude()));
        bookTitle.setText(pin.passage().getBook().getTitle());
        author.setText(pin.passage().getBook().getAuthor());
        year.setText(String.valueOf(pin.passage().getBook().getYear()));
        description.setText(pin.description());
        passage.setText(pin.passage().getText());
        status.setText(" ");

        dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Edit pin #" + pin.id(),
                Dialog.ModalityType.APPLICATION_MODAL);
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 6));
        addRow(fields, "Latitude", latitude);
        addRow(fields, "Longitude", longitude);
        addRow(fields, "Book", bookTitle);
        addRow(fields, "Author", author);
        addRow(fields, "Year", year);

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 8));
        texts.add(titled("Description", description));
        texts.add(titled("Passage", passage));

        JButton save = new JButton("Save");
        save.addActionListener(event -> controller.execute(pinId, pin.owner(),
                description.getText(), latitude.getText(), longitude.getText(), passage.getText(),
                bookTitle.getText(), author.getText(), year.getText()));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(event -> dialog.dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(save);
        buttons.add(cancel);

        JPanel south = new JPanel(new BorderLayout());
        south.add(status, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(fields, BorderLayout.NORTH);
        content.add(texts, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setSize(480, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Shows the outcome: closes the form on success, shows the error otherwise. */
    public void onResult(EditPinState state) {
        if (dialog == null || !dialog.isVisible()) {
            return;
        }
        if (state.isSuccess()) {
            dialog.dispose();
        } else {
            status.setForeground(Color.RED);
            status.setText(state.getMessage());
        }
    }

    private static void addRow(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label + ":"));
        panel.add(field);
    }

    private static JScrollPane titled(String title, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }
}