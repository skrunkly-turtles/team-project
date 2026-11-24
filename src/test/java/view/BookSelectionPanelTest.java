package view;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import models.Book;
import org.junit.jupiter.api.Test;
import use_case.select_location.SelectedLocation;
import userCase.LookUpBookDataAccessInterface;

/** Tests the library wrapper offline, including retained and discarded sessions. */
class BookSelectionPanelTest {
  /** Searching and reading use the library components; coordinate revision retains the reader. */
  @Test
  void searchesOpensReadsAndRetainsBookWhenReturning() throws Exception {
    AtomicInteger sessions = new AtomicInteger();
    AtomicInteger back = new AtomicInteger();
    AtomicInteger cancel = new AtomicInteger();
    Book book = new Book("Example book", "Example author", 2020, "example-id");
    book.setText("The complete example book text.");
    BookSelectionPanel[] panel = new BookSelectionPanel[1];
    SwingUtilities.invokeAndWait(() -> {
      panel[0] = new BookSelectionPanel(() -> {
        sessions.incrementAndGet();
        return library(book);
      }, back::incrementAndGet, cancel::incrementAndGet);
      panel[0].open(new SelectedLocation(43, -79));
      fields(panel[0], JTextField.class).get(0).setText("Example");
      button(panel[0], "Search").doClick(0);
    });
    awaitUi(() -> fields(panel[0], JList.class).get(0).getModel().getSize() > 0);
    SwingUtilities.invokeAndWait(() -> {
      fields(panel[0], JList.class).get(0).setSelectedIndex(0);
      button(panel[0], "Open").doClick(0);
    });
    awaitUi(() -> fields(panel[0], JTextArea.class).stream()
        .anyMatch(text -> text.getText().equals(book.getText())));
    SwingUtilities.invokeAndWait(() -> {
      assertTrue(fields(panel[0], views.ReaderView.class).get(0).isVisible());
      button(panel[0], "Back to location").doClick(0);
      assertEquals(1, back.get());
      panel[0].open(new SelectedLocation(44, -80));
      assertEquals(1, sessions.get());
      assertTrue(fields(panel[0], views.ReaderView.class).get(0).isVisible());
      assertTrue(fields(panel[0], JLabel.class).stream().anyMatch(label ->
          "Location: 44.00000, -80.00000".equals(label.getText())));
      button(panel[0], "Back to search").doClick(0);
    });
    SwingUtilities.invokeAndWait(() -> {
      assertTrue(fields(panel[0], views.LookUpBookView.class).get(0).isVisible());
      assertEquals("Example", fields(panel[0], JTextField.class).get(0).getText());
      button(panel[0], "Cancel Add Pin").doClick(0);
      assertEquals(1, cancel.get());
      panel[0].reset();
      panel[0].open(new SelectedLocation(1, 2));
      assertEquals(2, sessions.get());
      assertEquals("", fields(panel[0], JTextField.class).get(0).getText());
    });
  }

  /** A response arriving after cancellation can update only the discarded library components. */
  @Test
  void cancelledSearchCannotPopulateTheNextPin() throws Exception {
    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch finished = new CountDownLatch(1);
    Book book = new Book("Old draft book", "Author", 2020, "old-id");
    AtomicInteger sessions = new AtomicInteger();
    BookSelectionPanel[] panel = new BookSelectionPanel[1];
    try {
      SwingUtilities.invokeAndWait(() -> {
        panel[0] = new BookSelectionPanel(() -> {
          if (sessions.incrementAndGet() > 1) { return library(book); }
          return new LookUpBookDataAccessInterface() {
            /** Waits until the next draft has started before returning this response. */
            public List<Book> searchByTitle(String query) {
              started.countDown();
              try {
                if (!release.await(5, TimeUnit.SECONDS)) { throw new IllegalStateException("Timed out"); }
                return List.of(book);
              } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
              } finally { finished.countDown(); }
            }
            /** Returns this fixture's book. */
            public Book getById(String id) { return book; }
          };
        }, () -> {}, () -> {});
        panel[0].open(new SelectedLocation(1, 2));
        fields(panel[0], JTextField.class).get(0).setText("Old request");
        button(panel[0], "Search").doClick(0);
      });
      assertTrue(started.await(5, TimeUnit.SECONDS));
      SwingUtilities.invokeAndWait(() -> {
        panel[0].reset();
        panel[0].open(new SelectedLocation(3, 4));
      });
      release.countDown();
      assertTrue(finished.await(5, TimeUnit.SECONDS));
      SwingUtilities.invokeAndWait(() -> {
        assertEquals(0, fields(panel[0], JList.class).get(0).getModel().getSize());
        assertEquals("", fields(panel[0], JTextField.class).get(0).getText());
      });
    } finally { release.countDown(); }
  }

  /** Creates an offline provider for one known book. */
  private static LookUpBookDataAccessInterface library(Book book) {
    return new LookUpBookDataAccessInterface() {
      /** Returns a matching fixture without HTTP. */
      public List<Book> searchByTitle(String query) { return List.of(book); }
      /** Returns the already-loaded fixture text. */
      public Book getById(String id) { return book; }
    };
  }

  /** Waits for an observable UI outcome while background library work completes. */
  private static void awaitUi(BooleanSupplier ready) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    boolean[] result = {false};
    do {
      SwingUtilities.invokeAndWait(() -> result[0] = ready.getAsBoolean());
      if (result[0]) { return; }
      Thread.sleep(10);
    } while (System.nanoTime() < deadline);
    fail("Library UI did not reach the expected state");
  }

  /** Finds an action button by its visible label. */
  private static JButton button(Container root, String label) {
    return fields(root, JButton.class).stream().filter(b -> label.equals(b.getText()))
        .findFirst().orElseThrow();
  }

  /** Collects controls recursively without requiring a native window. */
  private static <T> List<T> fields(Container root, Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) { result.add(type.cast(component)); }
      if (component instanceof Container) { result.addAll(fields((Container) component, type)); }
    }
    return result;
  }
}
