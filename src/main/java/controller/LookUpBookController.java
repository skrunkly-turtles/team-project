package controller;
/*
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
        .url("https://api.example.com/weather?city=Toronto")
        .build();

try (Response response = client.newCall(request).execute()) {
    if (response.isSuccessful()) {
        String body = response.body().string();  // the response as text
        System.out.println(body);
    } else {
        System.out.println("Request failed with code " + response.code());
    }
} catch (IOException e) {
    // A network problem (no connection, timeout, ...) — handle it gracefully.
    e.printStackTrace();
}
    - FROM THE COURSE NOTES
    https://github.com/CSC207-UofT/207-course-notes/blob/main/apis-json-and-files.md#apis-json-and-files
 */

import inputBoundary.LookUpBookInputBoundary;
import inputBoundary.LookUpBookInputData;

/*
    User Agent: CSC207 Library App (taysen.victory@mail.utoronto.ca)
    Open Library: Book Search, Search Inside
    https://openlibrary.org/dev/docs/api/search

    https://openlibrary.org/search.json?q=the+lord+of+the+rings
 */
public class LookUpBookController {
    private final LookUpBookInputBoundary interactor;

    public LookUpBookController(LookUpBookInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String query) {
        LookUpBookInputData inputData = new LookUpBookInputData(query);
        interactor.execute(inputData);
    }

    public void openBook(String bookId) {
    }
}
