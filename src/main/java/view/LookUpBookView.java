package view;

import interface_adapter.lookup_book.LookUpBookController;
import interface_adapter.lookup_book.LookUpBookState;
import interface_adapter.lookup_book.LookupBookViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class LookUpBookView extends JPanel implements PropertyChangeListener {
    LookUpBookController controller;
    private final LookupBookViewModel<LookUpBookState> viewModel;
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");
    private final JButton openButton = new JButton("Open");
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final JLabel statusLabel = new JLabel();


    public LookUpBookView(LookUpBookController controller, LookupBookViewModel<LookUpBookState> viewModel) {
        this.controller = controller;
        this.viewModel = viewModel;
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        JPanel top = new JPanel();

        searchButton.addActionListener(event -> search());
        searchField.addActionListener(event -> search());
        openButton.addActionListener(event -> open());

        top.add(searchField);
        top.add(searchButton);
        top.add(openButton);

        for(String book: viewModel.getState().getResults()) {
            listModel.addElement(book);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    open();   // double-click opens the book
                }
            }
        });

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // May be called from a background thread, so update the screen on the Swing thread.
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            listModel.addAll(viewModel.getState().getResults());
            statusLabel.setText(viewModel.getState().getError());
        });
    }

    private void search() {
        String query = searchField.getText();
        statusLabel.setText("Searching...");
        new Thread(() -> controller.execute(query)).start();   // background thread for API call
    }

    private void open() {
        int index = list.getSelectedIndex();
        List<String> ids = viewModel.getState().getBookIds();
        if (index < 0 || index >= ids.size()) {
            statusLabel.setText("Select a book first");
            return;
        }
        String bookId = ids.get(index);
        statusLabel.setText("Opening book...");
        new Thread(() -> controller.openBook(bookId)).start();
    }
}
