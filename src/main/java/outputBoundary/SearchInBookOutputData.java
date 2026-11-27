package outputBoundary;

import java.util.List;

/** Where the text was found: every match (for highlighting) and the first few with surrounding text. */
public class SearchInBookOutputData {
    private final List<Integer> matchStarts;   // position of the first character of every match
    private final int matchLength;             // every match is as long as the search text
    private final List<String> snippets;       // the top matches with some text around them

    public SearchInBookOutputData(List<Integer> matchStarts, int matchLength, List<String> snippets) {
        this.matchStarts = matchStarts;
        this.matchLength = matchLength;
        this.snippets = snippets;
    }

    public List<Integer> getMatchStarts() { return matchStarts; }
    public int getMatchLength() { return matchLength; }
    public List<String> getSnippets() { return snippets; }
}
