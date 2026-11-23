package dataAccess;

import models.Book;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class LibraryApiDataAccessObjectTest {
    private static final String PRIDE_ID = "prideprejudice00aust";
    private static final String PRIDE_TEXT = "\"I must think your language too strong in speaking of both,\" replied Jane; \"and I hope you will be convinced of it by seeing them happy together. But enough of this. You alluded to something else. ";

    // What the fake "internet" answers with. Tests change these before calling the DAO.
    private String searchBody = searchResult();
    private int searchStatus = 200;
    private byte[] textBody = new byte[0];
    private final Queue<Integer> textStatuses = new ArrayDeque<>();   // used one per text request, then 200
    private boolean cantConnect = false;

    // What the DAO asked for
    private int searchRequests = 0;
    private int textRequests = 0;
    private String lastSearchQuery;
    private String lastTextPath;
    private String lastUserAgent;

    private LibraryApiDataAccessObject dao;

    /**
     * Gives the DAO a client that never touches the internet: an interceptor answers every request
     * itself. Requests to openlibrary.org get searchBody, requests to archive.org get textBody.
     * The DAO keeps its client in a private field, so it is swapped in with reflection.
     */
    @BeforeEach
    void setUp() throws Exception {
        Interceptor fakeInternet = chain -> {
            Request request = chain.request();
            if (cantConnect) {
                throw new IOException("no connection");
            }
            lastUserAgent = request.header("User-Agent");
            if (request.url().host().equals("openlibrary.org")) {
                searchRequests++;
                lastSearchQuery = request.url().queryParameter("q");
                return respond(request, searchStatus, searchBody.getBytes(StandardCharsets.UTF_8));
            }
            textRequests++;
            lastTextPath = request.url().encodedPath();
            Integer status = textStatuses.poll();
            return respond(request, status == null ? 200 : status, status == null ? textBody : new byte[0]);
        };

        dao = new LibraryApiDataAccessObject();
        Field clientField = LibraryApiDataAccessObject.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(dao, new OkHttpClient.Builder().addInterceptor(fakeInternet).build());
    }

    private static Response respond(Request request, int status, byte[] body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("fake")
                .body(ResponseBody.create(MediaType.parse("text/plain"), body))
                .build();
    }

    /** One entry of an Open Library search. Use null for a missing author or year, and no ids for no scan. */
    private static String book(String title, String author, Integer year, String... archiveIds) {
        String json = "{\"title\":\"" + title + "\"";
        if (author != null) {
            json += ",\"author_name\":[\"" + author + "\"]";
        }
        if (year != null) {
            json += ",\"first_publish_year\":" + year;
        }
        if (archiveIds.length > 0) {
            json += ",\"ia\":[\"" + String.join("\",\"", archiveIds) + "\"]";
        }
        return json + "}";
    }

    private static String searchResult(String... books) {
        return "{\"docs\":[" + String.join(",", books) + "]}";
    }

    /** Searches for Pride and Prejudice, opens it while the text file contains this, and returns the text we get. */
    private String textAfterOpening(String fileContents) {
        searchBody = searchResult(book("Pride and Prejudice", "Jane Austen", 1813, PRIDE_ID));
        textBody = fileContents.getBytes(StandardCharsets.UTF_8);
        dao.searchByTitle("pride");
        return dao.getById(PRIDE_ID).getText();
    }

    private static void assertContains(String expected, String actual) {
        assertNotNull(actual, "Expected a message containing \"" + expected + "\" but got null");
        assertTrue(actual.contains(expected),
                "Expected \"" + actual + "\" to contain \"" + expected + "\"");
    }

    @Test
    void searchWithResults_returnsBooksWithIds() {
        searchBody = searchResult(
                book("Pride and Prejudice", "Jane Austen", 1813, PRIDE_ID),
                book("Wonder", "R. J. Palacio", 2012, "wonder0000pala"));

        List<Book> books = dao.searchByTitle("pride");

        assertEquals(2, books.size());
        assertEquals("Pride and Prejudice by Jane Austen (1813)", books.get(0).toString());
        assertEquals(PRIDE_ID, books.get(0).getId());
        assertEquals("Wonder by R. J. Palacio (2012)", books.get(1).toString());
        assertEquals("wonder0000pala", books.get(1).getId());
    }

    @Test
    void searchWithNoResults_returnsEmptyList() {
        searchBody = searchResult();

        List<Book> books = dao.searchByTitle("Wonder");

        assertTrue(books.isEmpty());
    }

    @Test
    void searchWithNoUsableId_skipsThoseBooks() {
        searchBody = searchResult(
                book("Power in Global Governance", "Michael Barnett", 2005),               // no scan at all
                book("Zen and the Art of Motorcycle Maintenance", "Robert M. Pirsig", 1974, "bad id"),   // space is not allowed
                book("1984", "George Orwell", 1949, "../etc/passwd"),                      // would escape the URL
                book("Fahrenheit 451", "Ray Bradbury", 1953, "fahrenheit451000brad"));

        List<Book> books = dao.searchByTitle("anything");

        assertEquals(1, books.size());
        assertEquals("Fahrenheit 451", books.get(0).getTitle());
    }

    @Test
    void searchWithSeveralIds_usesFirstSafeOne() {
        searchBody = searchResult(book("Wonder", "R. J. Palacio", 2012, "not safe!", "wonder0000pala", "wonder-2"));

        List<Book> books = dao.searchByTitle("wonder");

        assertEquals("wonder0000pala", books.get(0).getId());
    }

    @Test
    void searchWithMissingAuthorAndYear_usesPlaceholders() {
        searchBody = searchResult(book("Chasing Shadows", null, null, "deibert25shadows"));

        List<Book> books = dao.searchByTitle("shadows");

        // No year from Open Library is stored as 0, so toString shows "(0)".
        assertEquals("Chasing Shadows by Unknown author (0)", books.get(0).toString());
    }

    @Test
    void searchWithOnlySymbols_returnsEmptyWithoutRequest() {
        List<Book> books = dao.searchByTitle(" !!! ??? ");

        assertTrue(books.isEmpty());
        assertEquals(0, searchRequests);
    }

    @Test
    void searchWithSymbols_stripsThemAndAsksForPublicBooks() {
        dao.searchByTitle("Pride & Prejudice!");

        assertEquals("Pride  Prejudice ebook_access:public", lastSearchQuery);
    }

    @Test
    void searchWithNumbers_keepsThem() {
        dao.searchByTitle("1984");

        assertEquals("1984 ebook_access:public", lastSearchQuery);
    }

    @Test
    void search_sendsUserAgent() {
        dao.searchByTitle("wonder");

        assertContains("CSC207", lastUserAgent);
    }

    @Test
    void searchServerError_throwsWithCode() {
        searchStatus = 500;

        RuntimeException error = assertThrows(RuntimeException.class, () -> dao.searchByTitle("wonder"));

        assertContains("failed with code 500", error.getMessage());
    }

    @Test
    void cantConnect_throwsReachError() {
        cantConnect = true;

        RuntimeException error = assertThrows(RuntimeException.class, () -> dao.searchByTitle("wonder"));

        assertContains("Could not reach", error.getMessage());
    }

    @Test
    void openBook_downloadsTextFromTheBooksId() {
        textAfterOpening(PRIDE_TEXT);

        assertEquals("/download/" + PRIDE_ID + "/" + PRIDE_ID + "_djvu.txt", lastTextPath);
    }

    @Test
    void openBookTwice_downloadsOnce() {
        textAfterOpening(PRIDE_TEXT);

        Book again = dao.getById(PRIDE_ID);

        assertEquals(1, textRequests);
        assertEquals(PRIDE_TEXT.trim(), again.getText());
    }

    @Test
    void openBookNeverSearched_throwsWithoutRequest() {
        RuntimeException error = assertThrows(RuntimeException.class, () -> dao.getById("not-a-real-id"));

        assertContains("search again", error.getMessage());
        assertEquals(0, textRequests);
    }

    @Test
    void openBookWithNullId_throwsWithoutRequest() {
        assertThrows(RuntimeException.class, () -> dao.getById(null));

        assertEquals(0, textRequests);
    }

    @Test
    void openBookFromOlderSearch_throws() {
        searchBody = searchResult(book("Pride and Prejudice", "Jane Austen", 1813, PRIDE_ID));
        dao.searchByTitle("pride");
        searchBody = searchResult(book("Wonder", "R. J. Palacio", 2012, "wonder0000pala"));
        dao.searchByTitle("wonder");   // replaces the remembered results

        assertThrows(RuntimeException.class, () -> dao.getById(PRIDE_ID));
    }

    @Test
    void openBookServerBusyOnce_retriesAndSucceeds() {
        textStatuses.add(503);

        String text = textAfterOpening(PRIDE_TEXT);

        assertEquals(PRIDE_TEXT.trim(), text);
        assertEquals(2, textRequests);
    }

    @Test
    void openBookServerAlwaysBusy_throwsWithCode() {
        textStatuses.add(503);
        textStatuses.add(503);
        textStatuses.add(503);

        RuntimeException error = assertThrows(RuntimeException.class, () -> textAfterOpening(PRIDE_TEXT));

        assertContains("failed with code 503", error.getMessage());
    }

    @Test
    void openBookMissingFile_throwsWithoutRetry() {
        textStatuses.add(404);

        RuntimeException error = assertThrows(RuntimeException.class, () -> textAfterOpening(PRIDE_TEXT));

        assertContains("failed with code 404", error.getMessage());
        assertEquals(1, textRequests);   // only 503 and 429 are worth trying again
    }

    @Test
    void openBookHugeFile_isCutOffWithNote() {
        String hugeFile = PRIDE_TEXT.repeat(110_000);   // about 21.7 MB, over the 20 MB limit

        String text = textAfterOpening(hugeFile);

        assertTrue(text.contains("The text was cut off here"));   // not assertContains: a failure would print 20 MB
        assertTrue(text.length() < hugeFile.length());
    }

    @Test
    void cleanText_rejoinsWordsSplitAcrossLines() {
        String text = textAfterOpening(PRIDE_TEXT.replace("speaking", "speak-\ning"));

        assertEquals(PRIDE_TEXT.trim(), text);
    }

    @Test
    void cleanText_joinsLinesInAParagraphButKeepsParagraphBreaks() {
        String text = textAfterOpening(PRIDE_TEXT.replace("Jane; ", "Jane;\n").replace("this. ", "this.\n\n"));

        assertEquals(PRIDE_TEXT.replace("this. ", "this.\n\n").trim(), text);
    }

    @Test
    void cleanText_removesLinesOfOnlySymbols() {
        String text = textAfterOpening(PRIDE_TEXT.replace("this. ", "this.\n\n~ ~ ~\n\n* * *\n\n"));

        assertEquals(PRIDE_TEXT.replace("this. ", "this.\n\n").trim(), text);
    }

    @Test
    void cleanText_collapsesRepeatedSpacesAndBlankLines() {
        String text = textAfterOpening(PRIDE_TEXT.replace("too strong", "too   strong").replace("this. ", "this.\n\n\n\n\n"));

        assertEquals(PRIDE_TEXT.replace("this. ", "this.\n\n").trim(), text);
    }

    @Test
    void cleanText_handlesWindowsLineEndings() {
        String text = textAfterOpening(PRIDE_TEXT.replace("Jane; ", "Jane;\r\n").replace("this. ", "this.\r\n\r\n"));

        assertEquals(PRIDE_TEXT.replace("this. ", "this.\n\n").trim(), text);
    }

    @Test
    void cleanText_emptyFile_staysEmpty() {
        String text = textAfterOpening("\n\n  \n");

        assertEquals("", text);
    }
}
