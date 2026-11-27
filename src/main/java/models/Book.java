package models;

public class Book {
    String title;
    String author;
    int year;
    String id = "";     // archive.org id used to look up the text; empty if unknown
    String text = "";   // the full text of the book; empty until the book has been opened

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }

    // Optional constructor to save the archive.org id without affecting other code that relies on Books
    public Book(String title, String author, int year, String id) {
        this(title, author, year);
        this.id = id;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + year + ")";
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
