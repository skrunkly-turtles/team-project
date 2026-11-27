package view;

import interface_adapter.delete_pin.DeletePinController;
import interface_adapter.delete_pin.DeletePinState;
import interface_adapter.delete_pin.DeletePinViewModel;
import javax.swing.JOptionPane;
import interface_adapter.edit_pin.EditPinController;
import interface_adapter.edit_pin.EditPinState;
import interface_adapter.edit_pin.EditPinViewModel;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.read_pins.ReadPinsController;
import interface_adapter.read_pins.ReadPinsState;
import interface_adapter.read_pins.ReadPinsViewModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import models.Pin.PinData;

/** Lists the pins the signed-in user may see by id; clicking one opens its details. */
public final class PinView extends JPanel {
    private final ReadPinsController controller;
    private final DefaultListModel<PinData> model = new DefaultListModel<>();
    private final JList<PinData> list = new JList<>(model);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final PinEditDialog editDialog;
    private final DeletePinController deleteController;
    private String currentUsername;

    /**
     * Creates the widget and subscribes it to the state it displays.
     *
     * @param controller loads the accessible pins
     * @param pinsModel observable pins state
     * @param profileModel observable profile state, used to reload when the profile changes
     */
    public PinView(ReadPinsController controller, ReadPinsViewModel pinsModel,
                   ProfileViewModel profileModel, EditPinController editController,
                   EditPinViewModel editModel, DeletePinController deleteController, DeletePinViewModel deletePinViewModel) {
        this.controller = controller;
        this.editDialog = new PinEditDialog(editController);
        this.deleteController = deleteController;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> source, Object value,
                                                          int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(source, value, index, selected, focused);
                setText("Pin #" + ((PinData) value).id());
                return this;
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int index = list.locationToIndex(event.getPoint());
                Rectangle bounds = index >= 0 ? list.getCellBounds(index, index) : null;
                if (bounds != null && bounds.contains(event.getPoint())) {
                    PinData pin = model.get(index);
                    boolean mine = pin.owner().equals(currentUsername);
                    PinDetailsDialogue.show(PinView.this, pin,
                            mine ? () -> editDialog.open(PinView.this, pin) : null,
                            mine ? () -> confirmDelete(pin) : null);
                }
            }
        });

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pinsModel.addPropertyChangeListener(
                event -> showPins((ReadPinsState) event.getNewValue()));
        profileModel.addPropertyChangeListener(
                event -> onProfileChanged((ProfileState) event.getNewValue()));
        editModel.addPropertyChangeListener(event -> {
            EditPinState state = (EditPinState) event.getNewValue();
            editDialog.onResult(state);
            if (state.isSuccess()) {
                controller.execute();
            }
        });

    }

    /** Reloads when a signed-in profile is shown, and clears everything on logout. */
    private void onProfileChanged(ProfileState state) {
        currentUsername = state.getUsername();
        if (state.getUsername() == null) {
            model.clear();
            statusLabel.setText(" ");
        } else {
            controller.execute();
        }
    }

    /** Replaces the displayed pins with the latest state. */
    private void showPins(ReadPinsState state) {
        model.clear();
        for (PinData pin : state.getPins()) {
            model.addElement(pin);
        }
        if (!state.getMessage().isEmpty()) {
            statusLabel.setText(state.getMessage());
        } else {
            statusLabel.setText(model.isEmpty() ? "No pins to show." : " ");
        }
    }
    /** Asks for confirmation, then deletes the pin. */
    private void confirmDelete(PinData pin) {
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete Pin #" + pin.id() + "? This cannot be undone.", "Delete pin",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            deleteController.execute(pin.id());
        }
    }
}