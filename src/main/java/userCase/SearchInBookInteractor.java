package userCase;

import inputBoundary.SearchInBookInputBoundary;
import inputBoundary.SearchInBookInputData;
import outputBoundary.SearchInBookOutputBoundary;
import outputBoundary.SearchInBookOutputData;

import java.util.ArrayList;
import java.util.List;

public class SearchInBookInteractor implements SearchInBookInputBoundary {
    private static final int SNIPPET_COUNT = 3;     // how many matches are listed with surrounding text
    private static final int CONTEXT_CHARS = 40;    // characters of surrounding text kept on each side of a match
    private static final int MAX_MATCHES = 2500;    // stop here so a very common word cannot flood the screen

    private final SearchInBookOutputBoundary presenter;

    public SearchInBookInteractor(SearchInBookOutputBoundary presenter) {
        this.presenter = presenter;
    }

    @Override
    public void execute(SearchInBookInputData inputData) {
        String query = inputData.getQuery() == null ? "" : inputData.getQuery().trim();
        if (query.isEmpty()) {
            presenter.prepareFailView("Please enter a search term");
            return;
        }

        String text = inputData.getBookText() == null ? "" : inputData.getBookText();
        List<Integer> matchStarts = findMatches(text, query);
        if (matchStarts.isEmpty()) {
            presenter.prepareFailView("No matches found for \"" + query + "\"");
            return;
        }

        List<String> snippets = new ArrayList<>();
        for (int i = 0; i < Math.min(SNIPPET_COUNT, matchStarts.size()); i++) {
            snippets.add(snippetAround(text, matchStarts.get(i), query.length()));
        }
        presenter.prepareSuccessView(new SearchInBookOutputData(matchStarts, query.length(), snippets));
    }

    /**
     * Start position of every match of query in text, in reading order. Ignores upper/lower case,
     * matches part of a word ("war" finds "warrior"), and never overlaps ("aa" in "aaaa" is found twice).
     * Stops after MAX_MATCHES. regionMatches is used instead of lower-casing the text so that
     * positions always line up with the original text.
     */
    static List<Integer> findMatches(String text, String query) {
        List<Integer> starts = new ArrayList<>();
        int length = query.length();
        for (int i = 0; i + length <= text.length() && starts.size() < MAX_MATCHES; i++) {
            if (text.regionMatches(true, i, query, 0, length)) {
                starts.add(i);
                i += length - 1;   // carry on after this match
            }
        }
        return starts;
    }

    /** The match with CONTEXT_CHARS of text on each side, on one line, with "..." where the text was cut. */
    static String snippetAround(String text, int start, int length) {
        int from = Math.max(0, start - CONTEXT_CHARS);
        int to = Math.min(text.length(), start + length + CONTEXT_CHARS);
        String snippet = text.substring(from, to).replaceAll("\\s+", " ").trim();
        return (from > 0 ? "..." : "") + snippet + (to < text.length() ? "..." : "");
    }
}
