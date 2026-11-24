package view;

import controller.LookUpBookController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import presenter.LookUpBookPresenter;
import use_case.select_location.SelectedLocation;
import userCase.LookUpBookDataAccessInterface;
import userCase.LookUpBookInteractor;
import views.LookUpBookState;
import views.LookUpBookView;
import views.ReaderState;
import views.ReaderView;
import views.ViewModel;
import views.ViewNames;

/** Hosts the library's search and reader while retaining an unsaved map location.
 * Library components are reused from #37.
 */
public final class BookSelectionPanel extends JPanel {
  private final Supplier<LookUpBookDataAccessInterface> libraryFactory;
  private final JLabel locationLabel = new JLabel();
  private JPanel libraryCards;

  /** Builds navigation around a separate library session for each new pin.
   * @param libraryFactory creates independent library data access for a new draft
   * @param onBack returns to the existing coordinate selection
   * @param onCancel discards the pin draft
   */
  public BookSelectionPanel(Supplier<LookUpBookDataAccessInterface> libraryFactory,
      Runnable onBack, Runnable onCancel) {
    super(new BorderLayout(8, 8));
    this.libraryFactory = Objects.requireNonNull(libraryFactory, "libraryFactory");
    JPanel header = new JPanel(new BorderLayout());
    header.add(new JLabel("Choose a book"), BorderLayout.NORTH);
    header.add(locationLabel, BorderLayout.CENTER);
    JPanel actions = new JPanel();
    JButton back = new JButton("Back to location");
    JButton cancel = new JButton("Cancel Add Pin");
    back.addActionListener(event -> onBack.run());
    cancel.addActionListener(event -> onCancel.run());
    actions.add(back);
    actions.add(cancel);
    header.add(actions, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);
  }

  /** Opens the library, preserving its search and reader when revising coordinates.
   * @param location latest confirmed location
   */
  public void open(SelectedLocation location) {
    Objects.requireNonNull(location, "location");
    if (libraryCards == null) {
      ViewModel<LookUpBookState> searchModel = new ViewModel<>(
          new LookUpBookState(new ArrayList<>(), ""));
      ViewModel<ReaderState> readerModel = new ViewModel<>(new ReaderState());
      ViewModel<String> libraryNavigation = new ViewModel<>(ViewNames.LOOKUP_VIEW);
      LookUpBookPresenter presenter = new LookUpBookPresenter(
          searchModel, readerModel, libraryNavigation);
      LookUpBookController controller = new LookUpBookController(
          new LookUpBookInteractor(libraryFactory.get(), presenter));
      CardLayout layout = new CardLayout();
      libraryCards = new JPanel(layout);
      libraryCards.add(new LookUpBookView(controller, searchModel), ViewNames.LOOKUP_VIEW);
      libraryCards.add(new ReaderView(readerModel, libraryNavigation), ViewNames.READER_VIEW);
      new views.ViewManager(libraryCards, layout, libraryNavigation);
      add(libraryCards, BorderLayout.CENTER);
    }
    locationLabel.setText(String.format(Locale.ROOT, "Location: %.5f, %.5f",
        location.getLatitude(), location.getLongitude()));
    // The parent CardLayout owns screen visibility; showing this panel here leaves the map visible too.
    revalidate();
    repaint();
  }

  /** Discards the library session so late responses cannot enter the next draft. */
  public void reset() {
    if (libraryCards != null) {
      remove(libraryCards);
      libraryCards = null;
    }
    locationLabel.setText("");
    revalidate();
    repaint();
  }
}
