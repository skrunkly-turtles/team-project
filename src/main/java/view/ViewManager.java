package view;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import interface_adapter.ViewManagerModel;

/** Replaces one Swing screen with another when the view manager model changes. */
public final class ViewManager extends JPanel implements PropertyChangeListener {
    private final CardLayout cards = new CardLayout();

    /** Connects a CardLayout to the observable navigation model. */
    public ViewManager(ViewManagerModel viewManagerModel) {
        setLayout(cards);
        viewManagerModel.addPropertyChangeListener(this);
    }

    /** Displays the named screen when navigation changes. */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        final String viewName = (String) event.getNewValue();
        // May be called from a background thread, so switch screens on the Swing thread.
        SwingUtilities.invokeLater(() -> cards.show(this, viewName));
    }
}