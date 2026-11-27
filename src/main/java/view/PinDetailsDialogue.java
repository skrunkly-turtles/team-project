package view;

import java.awt.BorderLayout;
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
import javax.swing.SwingUtilities;
import models.Pin.PinData;

/** Modal dialog showing everything stored on one pin. */
public final class PinDetailsDialogue {
    private PinDetailsDialogue() { }

    public static void show(Component parent, PinData pin) {
        show(parent, pin, null, null);
    }

    /** Shows the pin in a modal dialog over the window containing {@code parent}. */
    public static void show(Component parent, PinData pin, Runnable onEdit, Runnable onDelete) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Pin #" + pin.id(), Dialog.ModalityType.APPLICATION_MODAL);

        JPanel facts = new JPanel(new GridLayout(0, 2, 8, 6));
        addRow(facts, "Created by", "@" + pin.owner());
        addRow(facts, "Visibility", pin.visible() ? "Public" : "Private (friends only)");
        addRow(facts, "Book", pin.passage().getBook().getTitle());
        addRow(facts, "Author", pin.passage().getBook().getAuthor());
        addRow(facts, "Year", String.valueOf(pin.passage().getBook().getYear()));
        addRow(facts, "Word count", String.valueOf(pin.passage().getWordCount()));
        addRow(facts, "Latitude", String.valueOf(pin.coordinate().getLatitude()));
        addRow(facts, "Longitude", String.valueOf(pin.coordinate().getLongitude()));

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 8));
        texts.add(makeTextArea("Description", pin.description()));
        texts.add(makeTextArea("Passage", pin.passage().getText()));

        JButton close = new JButton("Close");
        close.addActionListener(event -> dialog.dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (onEdit != null) {
            JButton edit = new JButton("Edit");
            edit.addActionListener(event -> {
                dialog.dispose();
                onEdit.run();
            });
            buttons.add(edit);
        }
        if (onDelete != null) {
            JButton delete = new JButton("Delete");
            delete.addActionListener(event -> {
                dialog.dispose();
                onDelete.run();
            });
            buttons.add(delete);
        }
        buttons.add(close);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(facts, BorderLayout.NORTH);
        content.add(texts, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Adds a label and value pair to the facts grid. */
    private static void addRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        panel.add(new JLabel(value));
    }

    /** Builds a read-only, wrapping, scrollable text box with a titled border. */
    private static JScrollPane makeTextArea(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }
}