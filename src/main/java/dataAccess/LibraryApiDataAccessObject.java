package dataAccess;

import entity.Book;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import userCase.LookUpBookDataAccessInterface;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Talks to two services:
 *  - Open Library (search.json) to find freely readable books.
 *  - The Internet Archive (archive.org/download) to get the scanned text of a chosen book.
 */
public class LibraryApiDataAccessObject implements LookUpBookDataAccessInterface {
    private static final String SEARCH_URL = "https://openlibrary.org/search.json";
    private static final String TEXT_URL = "https://archive.org/download/";
    private static final String USER_AGENT = "CSC207 Library App (taysen.victory@mail.utoronto.ca)";
    private static final int RESULT_LIMIT = 15;
    private static final int MAX_TEXT_BYTES = 20_000_000;
    private static final int MAX_ATTEMPTS = 2; // tries when the server says it is busy (503's often)
    private static final String SAFE_ID = "[A-Za-z0-9._-]+"; // ids go into a URL, so allow only safe characters

    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();

    // The books from the most recent search, by id, so getById knows which book an id refers to.
    private final Map<String, Book> lastResults = new ConcurrentHashMap<>();

    @Override
    public List<Book> searchByTitle(String title) {
        // Keep letters, digits and spaces only, so the text cannot break the search syntax.
        String searchTerm = title.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        if (searchTerm.isEmpty()) { return new ArrayList<>(); }

        // "ebook_access:public" keeps only books that can be read freely.
        final HttpUrl finalUrl = HttpUrl.parse(SEARCH_URL).newBuilder()
                .addQueryParameter("q", searchTerm + " ebook_access:public")
                .addQueryParameter("limit", String.valueOf(RESULT_LIMIT))
                .addQueryParameter("fields", "key,title,author_name,first_publish_year,ia")
                .build();

        List<Book> books = convertToBooks(fetch(finalUrl));

        lastResults.clear();
        for (Book book : books) {
            lastResults.put(book.getId(), book);
        }
        return books;
    }

    private List<Book> convertToBooks(String body) {
        List<Book> books = new ArrayList<>();
        final JSONArray docs = new JSONObject(body).getJSONArray("docs");

        for (int i = 0; i < docs.length(); i++) {
            final JSONObject doc = docs.getJSONObject(i);
            final String title = doc.optString("title", "Unknown title");
            final JSONArray authors = doc.optJSONArray("author_name");
            final String authorName = authors != null && !authors.isEmpty()
                    ? authors.getString(0) : "Unknown author";
            final int publishYear = doc.optInt("first_publish_year", 0);

            final String id = firstSafeId(doc.optJSONArray("ia"));
            if (id == null) {
                continue;   // no scan to read, so we cannot offer this book
            }
            books.add(new Book(title, authorName, publishYear, id));
        }

        return books;
    }

    /** The first Internet Archive id in the list that is safe to put in a URL, or null. */
    private String firstSafeId(JSONArray ids) {
        for (int i = 0; ids != null && i < ids.length(); i++) {
            String id = ids.getString(i);
            if (id.matches(SAFE_ID)) {
                return id;
            }
        }
        return null;
    }

    /** Returns the book from the last search with its text loaded (downloading it the first time). */
    @Override
    public Book getById(String bookId) {
        Book book = (bookId == null) ? null : lastResults.get(bookId);
        if (book == null) {
            throw new RuntimeException("Book not found, please search again");
        }
        if (book.getText().isEmpty()) {
            HttpUrl url = HttpUrl.parse(TEXT_URL + bookId + "/" + bookId + "_djvu.txt");
            book.setText(clean(fetch(url)));
        }
        return book;
    }

    /** GETs a URL and returns the body as text; waits and retries if the server is busy. */
    private String fetch(HttpUrl url) {
        for (int attempt = 1; ; attempt++) {
            final Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                if (response.isSuccessful()) {
                    return readLimited(response);
                }
                if ((code == 503 || code == 429) && attempt < MAX_ATTEMPTS) {
                    pause(1500L * attempt);   // wait before trying again
                    continue;
                }
                throw new RuntimeException("Request to " + url.host() + " failed with code " + code);
            } catch (IOException e) {
                throw new RuntimeException("Could not reach " + url.host(), e);
            }
        }
    }

    /** Reads the response body as text, stopping at MAX_TEXT_BYTES. */
    private String readLimited(Response response) throws IOException {
        try (InputStream in = response.body().byteStream()) {
            byte[] data = in.readNBytes(MAX_TEXT_BYTES + 1);
            boolean tooLarge = data.length > MAX_TEXT_BYTES;
            String text = new String(data, 0, Math.min(data.length, MAX_TEXT_BYTES), StandardCharsets.UTF_8);
            return tooLarge ? text + "\n\n[The text was cut off here: the file is larger than the download limit.]" : text;
        }
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to retry", e);
        }
    }

    /**
     * Makes scanned text easier to read. It cannot fix words the OCR misread;
     * it only removes layout noise left over from the page images.
     */
    private static String clean(String text) {
        text = text.replace("\r", "");
        text = text.replaceAll("(\\w)-\\n(\\w)", "$1$2");        // rejoin words split across lines
        text = text.replaceAll("(?m)^[^\\p{L}\\p{N}]*$", "");    // blank out lines with no letters or digits
        text = text.replaceAll("(?<!\\n)\\n(?!\\n)", " ");       // single line breaks inside a paragraph -> space
        text = text.replaceAll("[ \\t]{2,}", " ");               // collapse repeated spaces
        text = text.replaceAll("\\n{3,}", "\n\n");               // at most one blank line between paragraphs
        return text.trim();
    }
}
