package views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything the reader screen shows. A state is never changed after it is made: the withXxx methods
 * return a new state, because the ViewModel only tells the screen when it is given a different object.
 */
public class ReaderState {
    private final String bookId;
    private final String title;
    private final String text;
    private final List<Integer> matchStarts;   // where every search match begins in the text
    private final int matchLength;             // how long every match is
    private final List<String> snippets;       // the top matches with surrounding text
    private final int currentMatch;            // which match is selected (index into matchStarts), -1 if none
    private final String message;              // search or passage feedback, "" when there is nothing to say

    public ReaderState() {
        this("", "", "");
    }

    public ReaderState(String title, String text) {
        this("", title, text);
    }

    // Optional constructor: also remembers which book is open, so searches and selections know where to look.
    public ReaderState(String bookId, String title, String text) {
        this(bookId, title, text, new ArrayList<>(), 0, new ArrayList<>(), -1, "");
    }

    private ReaderState(String bookId, String title, String text, List<Integer> matchStarts, int matchLength,
                        List<String> snippets, int currentMatch, String message) {
        this.bookId = bookId;
        this.title = title;
        this.text = text;
        this.matchStarts = matchStarts;
        this.matchLength = matchLength;
        this.snippets = snippets;
        this.currentMatch = currentMatch;
        this.message = message;
    }

    /** Same book, new search results; the first match is selected and any message is cleared. */
    public ReaderState withSearchResults(List<Integer> starts, int length, List<String> topSnippets) {
        return new ReaderState(bookId, title, text, starts, length, topSnippets, 0, "");
    }

    /** Same book, old search results removed, and this message shown (for example "No matches found"). */
    public ReaderState withNoMatches(String message) {
        return new ReaderState(bookId, title, text, new ArrayList<>(), 0, new ArrayList<>(), -1, message);
    }

    /** Same book and search results, a different match selected. */
    public ReaderState withCurrentMatch(int index) {
        return new ReaderState(bookId, title, text, matchStarts, matchLength, snippets, index, message);
    }

    /** Same book and search results, a different message. */
    public ReaderState withMessage(String message) {
        return new ReaderState(bookId, title, text, matchStarts, matchLength, snippets, currentMatch, message);
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public List<Integer> getMatchStarts() { return Collections.unmodifiableList(matchStarts); }
    public int getMatchLength() { return matchLength; }
    public List<String> getSnippets() { return Collections.unmodifiableList(snippets); }
    public int getCurrentMatch() { return currentMatch; }
    public String getMessage() { return message; }
}
