package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import interface_adapter.map.PinDetailsController;
import interface_adapter.map.PinDetailsViewModel;
import use_case.pin_details.PinCreationDraft;
import use_case.pin_details.PinCreationDraft.Visibility;
import use_case.pin_details.PinDetailsOutputData;

/** Optional image/thoughts and audience form beside the selected map location. */
public class PinDetailsPanel extends JPanel {
    private final PinDetailsController controller;
    private final PinDetailsViewModel viewModel;
    private final JTextArea thoughts = new JTextArea(5, 22);
    private final JLabel location = new JLabel();
    private final JLabel source = new JLabel();
    private final JTextArea passageText = new JTextArea(5, 22);
    private final JLabel imagePreview = new JLabel("No image attached");
    private final JLabel imageStatus = new JLabel("PNG/JPEG • up to 10 MB • 20 MP");
    private final ButtonGroup audience = new ButtonGroup();
    private final JRadioButton publicChoice = new JRadioButton("Public — everyone");
    private final JRadioButton privateChoice = new JRadioButton("Private — only you");
    private final JRadioButton friendsChoice = new JRadioButton("Friends only — you and friends");
    private String imagePath;
    private int imageRequest;
    private boolean rendering;

    /**
     * Builds the draft form and observes presentation state.
     * @param controller details request adapter
     * @param viewModel observable draft state
     */
    public PinDetailsPanel(PinDetailsController controller, PinDetailsViewModel viewModel) {
        super(new BorderLayout(8, 8));
        this.controller = controller;
        this.viewModel = viewModel;
        setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        setPreferredSize(new Dimension(350, 0));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Pin details"));
        form.add(location);
        form.add(source);
        passageText.setEditable(false);
        passageText.setLineWrap(true);
        passageText.setWrapStyleWord(true);
        JScrollPane passageScroll = new JScrollPane(passageText);
        passageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        form.add(passageScroll);
        form.add(Box.createVerticalStrut(14));
        form.add(new JLabel("Your thoughts (optional)"));
        thoughts.setLineWrap(true);
        thoughts.setWrapStyleWord(true);
        JScrollPane thoughtsScroll = new JScrollPane(thoughts);
        thoughtsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        form.add(thoughtsScroll);
        form.add(Box.createVerticalStrut(14));
        form.add(new JLabel("Location photo (optional)"));
        JPanel imageActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton attach = new JButton("Attach image");
        JButton remove = new JButton("Remove image");
        attach.addActionListener(event -> chooseImage());
        remove.addActionListener(event -> {
            imageRequest++;
            imagePath = null;
            imagePreview.setIcon(null);
            imagePreview.setText("No image attached");
            imageStatus.setText("PNG/JPEG • up to 10 MB • 20 MP");
            flushDraft();
        });
        imageActions.add(attach);
        imageActions.add(remove);
        form.add(imageActions);
        form.add(imagePreview);
        form.add(imageStatus);
        form.add(Box.createVerticalStrut(14));
        form.add(new JLabel("Who can see this pin?"));
        for (JRadioButton choice : new JRadioButton[] {publicChoice, privateChoice, friendsChoice}) {
            audience.add(choice);
            form.add(choice);
            choice.addActionListener(event -> { if (!rendering) { flushDraft(); } });
        }
        form.add(Box.createVerticalGlue());
        for (Component component : form.getComponents()) {
            if (component instanceof JComponent) {
                ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }
        JScrollPane scroll = new JScrollPane(form);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        JPanel actions = new JPanel();
        JButton back = new JButton("Back");
        JButton cancel = new JButton("Cancel");
        JButton create = new JButton("Create Pin");
        create.setEnabled(false);
        create.setToolTipText("Passage selection and saving will be connected before creation is enabled.");
        back.addActionListener(event -> { flushDraft(); controller.back(); });
        cancel.addActionListener(event -> controller.cancel());
        actions.add(back);
        actions.add(cancel);
        actions.add(create);
        add(actions, BorderLayout.SOUTH);
        viewModel.addPropertyChangeListener(event -> render());
        setVisible(false);
    }

    /** Copies current form entries into the unsaved draft. */
    private void flushDraft() {
        Visibility visibility = publicChoice.isSelected() ? Visibility.PUBLIC
                : privateChoice.isSelected() ? Visibility.PRIVATE
                : friendsChoice.isSelected() ? Visibility.FRIENDS_ONLY : null;
        controller.update(thoughts.getText(), imagePath, visibility);
    }

    /** Shows the latest draft without resetting entries during ordinary edits. */
    private void render() {
        PinDetailsOutputData state = viewModel.getState();
        boolean editing = state.getStage() == PinDetailsOutputData.Stage.EDITING;
        setVisible(editing);
        if (!editing) {
            imageRequest++;
            imageStatus.setText(imagePath == null ? "PNG/JPEG • up to 10 MB • 20 MP"
                    : new File(imagePath).getName());
        }
        PinCreationDraft draft = state.getDraft();
        if (draft == null) {
            thoughts.setText("");
            source.setText("");
            passageText.setText("");
            audience.clearSelection();
            imagePath = null;
            imagePreview.setIcon(null);
            imagePreview.setText("No image attached");
            imageStatus.setText("PNG/JPEG • up to 10 MB • 20 MP");
        } else if (editing) {
            rendering = true;
            try {
                location.setText(String.format(Locale.ROOT, "Location: %.5f, %.5f",
                        draft.getLocation().getLatitude(), draft.getLocation().getLongitude()));
                source.setText(draft.getPassage() == null ? "No passage selected"
                        : draft.getPassage().getBookTitle()
                        + (draft.getPassage().getAuthor().isEmpty() ? "" : " — " + draft.getPassage().getAuthor()));
                passageText.setText(draft.getPassage() == null ? "" : draft.getPassage().getText());
                passageText.setCaretPosition(0);
                if (!thoughts.getText().equals(draft.getThoughts())) { thoughts.setText(draft.getThoughts()); }
                audience.clearSelection();
                if (draft.getVisibility() == Visibility.PUBLIC) { publicChoice.setSelected(true); }
                if (draft.getVisibility() == Visibility.PRIVATE) { privateChoice.setSelected(true); }
                if (draft.getVisibility() == Visibility.FRIENDS_ONLY) { friendsChoice.setSelected(true); }
                imagePath = draft.getImagePath();
            } finally { rendering = false; }
        }
        if (getParent() != null) { getParent().revalidate(); getParent().repaint(); }
    }

    /** Opens the local file chooser without copying or uploading the file. */
    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG and JPEG images", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadImage(chooser.getSelectedFile().toPath());
        }
    }

    /**
     * Loads the selected image off Swing's event thread; stale results are discarded.
     * @param path local image selected by the user
     */
    void loadImage(Path path) {
        int request = ++imageRequest;
        imageStatus.setText("Loading image…");
        new SwingWorker<BufferedImage, Void>() {
            /** Decodes the image on a background thread. */
            @Override
            protected BufferedImage doInBackground() throws Exception { return LocalImagePreview.read(path); }
            /** Applies a successful preview only to the current, still-open form. */
            @Override
            protected void done() {
                if (request != imageRequest || !isVisible()) { return; }
                try {
                    BufferedImage thumbnail = get();
                    imagePath = path.toAbsolutePath().toString();
                    imagePreview.setIcon(new ImageIcon(thumbnail));
                    imagePreview.setText("");
                    imageStatus.setText(new File(imagePath).getName());
                    flushDraft();
                } catch (Exception exception) {
                    imageStatus.setText("Cannot preview image. Choose a PNG/JPEG within the size limits.");
                }
            }
        }.execute();
    }
}
