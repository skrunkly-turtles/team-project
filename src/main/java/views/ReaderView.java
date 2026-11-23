package views;

import controller.SearchInBookController;
import controller.SelectPassageController;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the full text of the opened book, with a button back to the search screen,
 * a search box for finding text inside the book, and a button to keep the selected text as a passage.
 */
public class ReaderView extends JPanel implements PropertyChangeListener {
    private static final Highlighter.HighlightPainter MATCH_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 235, 130));     // every match
    private static final Highlighter.HighlightPainter CURRENT_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 160, 60));      // the match we are on
    private static final Highlighter.HighlightPainter PASSAGE_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(190, 130, 255));      // the match we are on

    private final ViewModel<ReaderState> viewModel;
    private final JLabel titleLabel = new JLabel();
    private final JButton backButton = new JButton("Back to search");
    private final JTextArea textArea = new JTextArea();

    private final SearchInBookController searchController;          // null if searching inside the book is not wired up
    private final SelectPassageController selectPassageController;  // null if selecting passages is not wired up
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Find");
    private final JButton previousButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JLabel matchCountLabel = new JLabel();               // for example "2 of 12"
    private final DefaultListModel<String> snippetModel = new DefaultListModel<>();
    private final JList<String> snippetList = new JList<>(snippetModel);   // the top matches with surrounding text
    private final JScrollPane snippetScroll = new JScrollPane(snippetList);
    private final JButton selectPassageButton = new JButton("Select passage");
    private final JLabel messageLabel = new JLabel(" ");                // search and passage feedback
    private String shownText = "";        // the book text currently in textArea, so a search does not reload it
    private int shownMatchStart = -1;     // the match the screen last scrolled to, so other updates do not scroll again
    private boolean updatingSnippets = false;   // true while we fill snippetList ourselves, so it is not treated as a click
    private final List<Object> searchHighlights = new ArrayList<>();   // tags of the search highlights, so passage highlights are left alone

    public ReaderView(ViewModel<ReaderState> viewModel, ViewModel<String> viewManagerModel) {
        this(viewModel, viewManagerModel, null, null);
    }

    // Optional constructor: also lets the reader search inside the book and select passages.
    public ReaderView(ViewModel<ReaderState> viewModel, ViewModel<String> viewManagerModel,
                      SearchInBookController searchController, SelectPassageController selectPassageController) {
        this.viewModel = viewModel;
        this.searchController = searchController;
        this.selectPassageController = selectPassageController;
        viewModel.addPropertyChangeListener(this);

        backButton.addActionListener(event -> viewManagerModel.setState(ViewNames.LOOKUP_VIEW));
        searchButton.addActionListener(event -> search());
        searchField.addActionListener(event -> search());
        nextButton.addActionListener(event -> showNextMatch());
        previousButton.addActionListener(event -> showPreviousMatch());
        selectPassageButton.addActionListener(event -> selectPassage());
        selectPassageButton.setFocusable(false);   // so clicking it does not hide the selection in the text
        snippetList.addListSelectionListener(event -> {
            int index = snippetList.getSelectedIndex();   // snippet number n is match number n
            if (!updatingSnippets && !event.getValueIsAdjusting() && index >= 0) { showMatch(index); }
        });
        previousButton.setEnabled(false);
        nextButton.setEnabled(false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                textArea.setCaretPosition(0);   // each time a book is opened, start at the top
            }
        });

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SERIF, Font.PLAIN, 16));
        textArea.setMargin(new Insets(12, 16, 12, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.add(backButton, BorderLayout.WEST);
        top.add(titleLabel, BorderLayout.CENTER);
        top.add(selectPassageButton, BorderLayout.EAST);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Find in book:"));
        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(previousButton);
        searchRow.add(nextButton);
        searchRow.add(matchCountLabel);

        snippetList.setVisibleRowCount(3);
        snippetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        snippetScroll.setPreferredSize(new Dimension(0, 66));   // room for the three snippets
        snippetScroll.setVisible(false);                        // only shown when there are matches

        JPanel searchArea = new JPanel(new BorderLayout());
        searchArea.add(searchRow, BorderLayout.NORTH);
        searchArea.add(snippetScroll, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(searchArea, BorderLayout.CENTER);

        messageLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        JLabel note = new JLabel("Scanned (OCR) text - it may contain errors or missing pages.");
        note.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel south = new JPanel(new BorderLayout());
        south.add(messageLabel, BorderLayout.NORTH);
        south.add(note, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    /** Asks for the text in the search box to be found in the open book (off the Swing thread, like the search screen). */
    private void search() {
        if (searchController == null) { return; }
        String bookText = viewModel.getState().getText();
        String query = searchField.getText();
        new Thread(() -> searchController.execute(bookText, query)).start();
    }

    /** Asks for the text selected on screen to be kept as a passage. */
    private void selectPassage() {
        if (selectPassageController == null) { return; }
        String bookId = viewModel.getState().getBookId();
        int start = textArea.getSelectionStart();
        int end = textArea.getSelectionEnd();
        try {
            Highlighter highlighter = textArea.getHighlighter();
            highlighter.addHighlight(start, end, PASSAGE_PAINTER);
        } catch (BadLocationException e) {
            // The highlighting failed, likely overlap
        }

        new Thread(() -> selectPassageController.execute(bookId, start, end)).start();
    }

    /** Moves to the next match, going back to the first after the last. */
    private void showNextMatch() {
        ReaderState state = viewModel.getState();
        int count = state.getMatchStarts().size();
        if (count > 0) { viewModel.setState(state.withCurrentMatch((state.getCurrentMatch() + 1) % count)); }
    }

    /** Moves to the previous match, going to the last from the first. */
    private void showPreviousMatch() {
        ReaderState state = viewModel.getState();
        int count = state.getMatchStarts().size();
        if (count > 0) { viewModel.setState(state.withCurrentMatch((state.getCurrentMatch() - 1 + count) % count)); }
    }

    /** Moves to the match with this number (for example when its snippet is clicked). */
    private void showMatch(int index) {
        ReaderState state = viewModel.getState();
        if (index < state.getMatchStarts().size()) { viewModel.setState(state.withCurrentMatch(index)); }
    }

    /** Shows the snippets, the "2 of 12" counter, the message, and enables Previous and Next when there is more than one match. */
    private void refreshSearchPanel(ReaderState state) {
        int count = state.getMatchStarts().size();

        updatingSnippets = true;
        snippetModel.clear();
        for (String snippet : state.getSnippets()) {
            snippetModel.addElement(snippet);
        }
        if (state.getCurrentMatch() >= 0 && state.getCurrentMatch() < snippetModel.size()) {
            snippetList.setSelectedIndex(state.getCurrentMatch());   // the snippet of the match we are on
        } else {
            snippetList.clearSelection();
        }
        updatingSnippets = false;

        snippetScroll.setVisible(count > 0);
        matchCountLabel.setText(count > 0 ? "  " + (state.getCurrentMatch() + 1) + " of " + count : "");
        messageLabel.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
        previousButton.setEnabled(count > 1);
        nextButton.setEnabled(count > 1);
        revalidate();
    }

    /** Highlights every match, with the match we are on in a different colour. */
    private void highlightMatches(ReaderState state) {
        Highlighter highlighter = textArea.getHighlighter();
        searchHighlights.forEach(highlighter::removeHighlight);   // only the search highlights, not the purple passage ones
        searchHighlights.clear();
        List<Integer> starts = state.getMatchStarts();
        try {
            for (int i = 0; i < starts.size(); i++) {
                int start = starts.get(i);
                searchHighlights.add(highlighter.addHighlight(start, start + state.getMatchLength(),
                        i == state.getCurrentMatch() ? CURRENT_PAINTER : MATCH_PAINTER));
            }
        } catch (BadLocationException e) {
            // The positions were found in this same text, so this should not happen; show what was highlighted so far.
        }
    }

    /** Scrolls to the match we are on, but only when it is a different one, so saving a passage does not move the text. */
    private void scrollToCurrentMatch(ReaderState state) {
        boolean hasCurrent = state.getCurrentMatch() >= 0 && state.getCurrentMatch() < state.getMatchStarts().size();
        int start = hasCurrent ? state.getMatchStarts().get(state.getCurrentMatch()) : -1;
        if (start >= 0 && start != shownMatchStart) {
            textArea.setCaretPosition(start);
        }
        shownMatchStart = start;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // May be called from a background thread, so update the screen on the Swing thread.
        SwingUtilities.invokeLater(() -> {
            ReaderState state = viewModel.getState();
            titleLabel.setText("  " + state.getTitle());
            if (!state.getText().equals(shownText)) {   // a search changes the state but not the book, so leave the text alone
                textArea.setText(state.getText());
                textArea.getHighlighter().removeAllHighlights();   // every highlight belongs to the previous book
                searchHighlights.clear();
                textArea.setCaretPosition(0);   // start at the top
                shownText = state.getText();
            }
            refreshSearchPanel(state);
            highlightMatches(state);
            scrollToCurrentMatch(state);
        });
    }
}
