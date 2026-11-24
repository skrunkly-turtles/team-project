package use_case.pin_details;

/** Immutable book metadata and passage text received by the unsaved creation flow. */
public final class SelectedPassage {
  private final String bookTitle;
  private final String author;
  private final String text;

  /** Captures the library result without retaining a mutable Book or Pin entity.
   * @param bookTitle title of the source book
   * @param author source author, or null when unavailable
   * @param text selected nonblank passage
   */
  public SelectedPassage(String bookTitle, String author, String text) {
    if (bookTitle == null || bookTitle.isBlank() || text == null || text.isBlank()) {
      throw new IllegalArgumentException("A book title and passage are required.");
    }
    this.bookTitle = bookTitle.trim();
    this.author = author == null ? "" : author.trim();
    this.text = text.trim();
  }

  /** @return source book title */
  public String getBookTitle() { return bookTitle; }
  /** @return source author, empty when unavailable */
  public String getAuthor() { return author; }
  /** @return selected passage text */
  public String getText() { return text; }
}
