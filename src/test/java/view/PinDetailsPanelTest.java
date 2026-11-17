package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import interface_adapter.map.*;
import use_case.pin_details.*;
import use_case.select_location.SelectedLocation;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises form actions through the real controller/presenter pipeline. */
class PinDetailsPanelTest {
    @TempDir Path directory;

    /** Back retains entries; cancel clears the form and Create remains disabled in preview. */
    @Test
    void preservesAndDiscardsFormEntries() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PinDetailsViewModel model = new PinDetailsViewModel();
            PinDetailsController controller = new PinDetailsController(new PinDetailsInteractor(new PinDetailsPresenter(model)));
            PinDetailsPanel panel = new PinDetailsPanel(controller, model);
            assertFalse(panel.isVisible());
            controller.open(new SelectedLocation(43, -79));
            assertTrue(panel.isVisible());
            JTextArea thoughts = (JTextArea) find(panel, JTextArea.class, null);
            thoughts.setText("Remember this spot");
            AbstractButton friends = (AbstractButton) find(panel, JRadioButton.class, "Friends only — you and friends");
            friends.doClick(0);
            assertEquals(PinCreationDraft.Visibility.FRIENDS_ONLY, model.getState().getDraft().getVisibility());
            JButton create = (JButton) find(panel, JButton.class, "Create Pin");
            assertFalse(create.isEnabled());
            ((JButton) find(panel, JButton.class, "Back")).doClick(0);
            assertFalse(panel.isVisible());
            controller.open(new SelectedLocation(44, -80));
            assertEquals("Remember this spot", thoughts.getText());
            assertTrue(friends.isSelected());
            ((JButton) find(panel, JButton.class, "Cancel")).doClick(0);
            assertNull(model.getState().getDraft());
            assertEquals("", thoughts.getText());
            assertFalse(friends.isSelected());
            assertFalse(panel.isVisible());
        });
    }

    /** A selected image is previewed asynchronously, retained on Back, and removable. */
    @Test
    void imagePreviewAndRemoval() throws Exception {
        Path image = directory.resolve("image.png");
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        PinDetailsViewModel model = new PinDetailsViewModel();
        PinDetailsController controller = new PinDetailsController(new PinDetailsInteractor(new PinDetailsPresenter(model)));
        PinDetailsPanel[] panel = new PinDetailsPanel[1];
        CountDownLatch loaded = new CountDownLatch(1);
        model.addPropertyChangeListener(event -> {
            if (model.getState().getDraft() != null && model.getState().getDraft().getImagePath() != null) {
                loaded.countDown();
            }
        });
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new PinDetailsPanel(controller, model);
            controller.open(new SelectedLocation(1, 2));
            panel[0].loadImage(image);
        });
        assertTrue(loaded.await(5, TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(image.toAbsolutePath().toString(), model.getState().getDraft().getImagePath());
            controller.back();
            controller.open(new SelectedLocation(3, 4));
            assertEquals(image.toAbsolutePath().toString(), model.getState().getDraft().getImagePath());
            ((JButton) find(panel[0], JButton.class, "Remove image")).doClick(0);
            assertNull(model.getState().getDraft().getImagePath());
        });
    }

    /**
     * Finds a control in nested form containers.
     * @param root starting container
     * @param type component type
     * @param text button text, or null
     * @return matching component, or null
     */
    private static Component find(Container root, Class<?> type, String text) {
        for (Component child : root.getComponents()) {
            if (type.isInstance(child) && (text == null
                    || child instanceof AbstractButton && ((AbstractButton) child).getText().equals(text))) { return child; }
            if (child instanceof Container) {
                Component result = find((Container) child, type, text);
                if (result != null) { return result; }
            }
        }
        return null;
    }
}
