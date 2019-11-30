package retrieval;
import java.util.ArrayList;

public class LDA extends Models {

    public LDA() {
        super();
    }

    private static double termFrequency(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = get_doc_indicies().get(term).getFileOccurrences().stream()
                .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
                .getOccurrences();

        return (double) occurrences / totalTerms;
    }

    private static double inverseDocumentFrequency(String term) {
        int totalDocuments = fileTermSize.keySet().size();
        int documentsWithTerm = documents.get(term).getSize();
        return Math.log((double) totalDocuments / documentsWithTerm);
    }

    public static double tfidf(String term, String document) {
        return occursInDocuments(term) ? termFrequency(term, document) * inverseDocumentFrequency(term) : 0;
    }

    public ArrayList<Similarity> retrieve(String query) {
        
        return null;
    }

}