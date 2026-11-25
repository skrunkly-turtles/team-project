package interface_adapter.lookup_book;

public class ReaderState {
    private String title = "";
    private String text = "";

    public ReaderState() {
    }

    public ReaderState(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() { return title; }
    public String getText() { return text; }
}
