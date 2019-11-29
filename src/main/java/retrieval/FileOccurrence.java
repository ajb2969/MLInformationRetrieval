package retrieval;

public class FileOccurrence {

    private String filename;
    private int occurrences;

    public FileOccurrence(String filename, int occurrences) {

        this.filename = filename;
        this.occurrences = occurrences;
    }

    public String getFilename() {
        return filename;
    }

    public int getOccurrences() {
        return occurrences;
    }
}
