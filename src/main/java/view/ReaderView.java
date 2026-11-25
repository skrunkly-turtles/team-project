package view;

import interface_adapter.lookup_book.ReaderState;
import interface_adapter.lookup_book.LookupBookViewModel;
import interface_adapter.ViewManagerModel;
import models.ViewNames;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/** Shows the full text of the opened book, with a button back to the search screen. */
public class ReaderView extends JPanel implements PropertyChangeListener {
    private final LookupBookViewModel<ReaderState> viewModel;
    private final JLabel titleLabel = new JLabel();
    private final JButton backButton = new JButton("Back to search");
    private final JTextArea textArea = new JTextArea();

    public ReaderView(LookupBookViewModel<ReaderState> viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        viewModel.addPropertyChangeListener(this);

        backButton.addActionListener(event -> viewManagerModel.show(ViewNames.LOOKUP_VIEW));

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SERIF, Font.PLAIN, 16));
        textArea.setMargin(new Insets(12, 16, 12, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.add(backButton, BorderLayout.WEST);
        top.add(titleLabel, BorderLayout.CENTER);

        JLabel note = new JLabel("Scanned (OCR) text - it may contain errors or missing pages.");
        note.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(note, BorderLayout.SOUTH);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // May be called from a background thread, so update the screen on the Swing thread.
        SwingUtilities.invokeLater(() -> {
            ReaderState state = viewModel.getState();
            titleLabel.setText("  " + state.getTitle());
            textArea.setText(state.getText());
            textArea.setCaretPosition(0);   // start at the top
        });
    }
}
