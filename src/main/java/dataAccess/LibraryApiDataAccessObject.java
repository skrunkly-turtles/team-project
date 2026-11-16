package dataAccess;

import entity.Book;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import userCase.LookUpBookDataAccessInterface;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class LibraryApiDataAccessObject implements LookUpBookDataAccessInterface {
    private static final String SEARCH_URL = "https://openlibrary.org/search.json";
    private static final String USER_AGENT = "CSC207 Library App (taysen.victory@mail.utoronto.ca)";
    private static final int RESULT_LIMIT = 5;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<Book> searchByTitle(String title) {
//        return null;
        String searchTerm = title.toLowerCase();
        searchTerm = searchTerm.replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "+");
        List<Book> books = new ArrayList<>();

        final HttpUrl finalUrl = HttpUrl.parse(SEARCH_URL).newBuilder()
                .addQueryParameter("q", searchTerm)
                .addQueryParameter("limit", String.valueOf(RESULT_LIMIT))
                .addQueryParameter("fields", "key,title,author_name,first_publish_year")
                .build();
        final Request request = new Request.Builder()
                .url(finalUrl)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String body = response.body().string();  // the response as text
                books = convertToBooks(body);

            } else {
                System.out.println("Request failed with code " + response.code());
            }
        } catch (IOException e) {
            // A network problem (no connection, timeout, ...) — handle it gracefully.
            e.printStackTrace();
        }
        return books;
    }

    private List<Book> convertToBooks(String body) {
        List<Book> books = new ArrayList<>();
        final JSONArray docs = new JSONObject(body).getJSONArray("docs");

        for (int i = 0; i < docs.length(); i++) {
            final JSONObject doc = docs.getJSONObject(i);
            final String title = doc.getString("title");
            final String authorName = doc.getJSONArray("author_name").get(0).toString();
            final int publishYear = doc.getInt("first_publish_year");

            books.add(new Book(title, authorName, publishYear));
        }

        return books;
    }

    @Override
    public Book getById(String bookId) {
        return null;
    }
}
