package views;

import controller.LookUpBookController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LookUpBookView extends JPanel implements LookUpBookViewModelListener {
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");

    private final DefaultListModel<String> listModel = new DefaultListModel<>();


    public LookUpBookView(LookUpBookViewModel viewModel) {
        setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.add(searchField);
        top.add(searchButton);

        for(String book: viewModel.getState().getResults()) {
            listModel.addElement(book);
        }

        JPanel results = new JPanel();
        JList<String> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.add(list);

        add(top, BorderLayout.NORTH);
        add(results, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent event) {
    }

    @Override
    public void stateChanged() {
    }

    public void setLookUpBookController(LookUpBookController controller) {
    }
}
