package outputBoundary;

/** What was saved: the passage text and how many words it has. */
public class SelectPassageOutputData {
    private final String passageText;
    private final int wordCount;

    public SelectPassageOutputData(String passageText, int wordCount) {
        this.passageText = passageText;
        this.wordCount = wordCount;
    }

    public String getPassageText() { return passageText; }
    public int getWordCount() { return wordCount; }
}
