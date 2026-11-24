package views;

import javax.swing.*;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/** Shows the screen named by the view-manager state (screens are cards in one panel). */
public class ViewManager implements PropertyChangeListener {
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final ViewModel<String> viewManagerModel;

    public ViewManager(JPanel cardPanel, CardLayout cardLayout, ViewModel<String> viewManagerModel) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.viewManagerModel = viewManagerModel;
        viewManagerModel.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // May be called from a background thread, so switch screens on the Swing thread.
        SwingUtilities.invokeLater(() -> cardLayout.show(cardPanel, viewManagerModel.getState()));
    }
}
