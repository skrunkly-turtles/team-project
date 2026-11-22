package view;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Home navigation layout with injected feature actions. */
public class HomeSidebar extends JPanel {
    private final JButton addPin = new JButton("Add Pin");

    /**
     * Builds sidebar buttons; application wiring supplies their actions.
     * @param onAddPin opens the location-selection step
     * @param onFilterPins opens the pin-filter controls
     * @param onProfile opens the authenticated user's profile
     * @param onLogout requests logout through the account controller
     */
    public HomeSidebar(Runnable onAddPin, Runnable onFilterPins, Runnable onProfile, Runnable onLogout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        setPreferredSize(new Dimension(180, 0));
        JLabel title = new JLabel("Passage Map");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(24));
        JButton filterPins = new JButton("Filter Pins");
        JButton profile = new JButton("Profile");
        JButton logout = new JButton("Log Out");
        addPin.addActionListener(event -> onAddPin.run());
        filterPins.addActionListener(event -> onFilterPins.run());
        profile.addActionListener(event -> onProfile.run());
        logout.addActionListener(event -> onLogout.run());
        for (JButton button : new JButton[] {addPin, filterPins}) {
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            add(button);
            add(Box.createVerticalStrut(10));
        }
        add(Box.createVerticalGlue());
        for (JButton button : new JButton[] {profile, logout}) {
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            add(button);
            if (button == profile) {
                add(Box.createVerticalStrut(10));
            }
        }
    }

    /**
     * Prevents starting a second draft while pin creation is active.
     * @param active whether a creation draft is currently open
     */
    public void setCreationActive(boolean active) {
        addPin.setEnabled(!active);
    }
}
