package retrieval;

import query.Application;


import java.util.*;


abstract public class Models {
    static final int RESULT_SET_SIZE = 30;
    public static final HashMap<String, String> HTMLDOCPATHS = Application.getHTMLDOCPATHS();//parseDocNumMap();
    private static final HashMap<String, Integer> FILE_TERM_SIZE = Application.getFILE_TERM_SIZE();//parseDocumentIndexFile(FILE_TERM_SIZE_PATH);
    private static final HashMap<String, Entry> TOKEN_TO_ENTRY = Application.getTOKEN_TO_ENTRY();//parseTokenIndexFile();
    private static final Map<String, Map<String, Integer>> TERM_TO_FILE_AND_OCCURRENCE = Application.getTERM_TO_FILE_AND_OCCURRENCE();//collapseIndexIntoMaps();
    private static final Map<String, Integer> DOCUMENT_SIZES= Application.getDOCUMENT_SIZES();//getSizeIndex();

    Models() {}

    public abstract ArrayList<Similarity> retrieve(String query);

    static double tfidf(String term, String document) {
        return occursInDocuments(term) ? termFrequency(term, document) * inverseDocumentFrequency(term) : 0;
    }

    static Set<String> getAllDocuments() {
        return FILE_TERM_SIZE.keySet();
    }

    static HashMap<String, Integer> getFileTermSize() {
        return FILE_TERM_SIZE;
    }

    static HashMap<String, Entry> getTokenToEntryIndex() {
        return TOKEN_TO_ENTRY;
    }

    static Map<String, Integer> getDocumentTokenSizes() {
        return DOCUMENT_SIZES;
    }

    static boolean occursInDocuments(String term) {
        return TOKEN_TO_ENTRY.containsKey(term);
    }

    int getOccurrencesInFile(String term, String filename) {
        return TERM_TO_FILE_AND_OCCURRENCE.getOrDefault(term, new HashMap<>()).getOrDefault(filename, 0);
    }

    /**
     * @return the termToFileAndOccurrence
     */
    public static Map<String, Map<String, Integer>> getTermToFileAndOccurrence() {
        return TERM_TO_FILE_AND_OCCURRENCE;
    }

    static String[] extractTerms(String query) {
        return query.toLowerCase().split("\\s+");
    }
    
    static double termFrequency(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = getTokenToEntryIndex().get(term).getFileOccurrences().stream()
            .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
            .getOccurrences();

        return (double) occurrences / totalTerms;
    }

    static TFSmoothing termFreqencySmoothing(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = occursInDocuments(term) ? getTokenToEntryIndex().get(term).getFileOccurrences().stream()
            .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
            .getOccurrences(): 0;

        return new TFSmoothing(totalTerms, occurrences);
    }

    private static double inverseDocumentFrequency(String term) {
        int totalDocuments = getFileTermSize().keySet().size();
        int documentsWithTerm = getTokenToEntryIndex().get(term).getSize();
        return Math.log((double) totalDocuments / documentsWithTerm);
    }



    static class TFSmoothing {
        private int occurences;
        private int terms;
        private double mu;
        private double score;

        TFSmoothing(int totalDocTokens, int tokenOccurences) {
            this.terms = totalDocTokens;
            this.occurences = tokenOccurences;
        }

        void setSmoothing(int numOccurencesInCollection, int numberTokensInCollection) {
            //term frequency of a word in a document + mu(collection frequency of a word/total number of words in a collection)
            //divided by the length of the document + mu
            this.score = (double)(occurences + (mu * ((double)numOccurencesInCollection/ numberTokensInCollection)))/(terms + mu);
        }

        /**
         * @param mu the mu to set
         */
        public void setMu(double mu) {
            this.mu = mu;
        }

        public void setScore(double newScore) {
            this.score = Math.log10(this.score) + newScore;
        }

        

        /**
         * @return the score
         */
        public double getScore() {
            return score;
        }
    }

    public static class Entry {
        private int size;
        private ArrayList<FileOccurrence> fileOccurrences;

        public Entry(int size, ArrayList<FileOccurrence> fileOccurrences) {
            this.size = size;
            this.fileOccurrences = fileOccurrences;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public ArrayList<FileOccurrence> getFileOccurrences() {
            return fileOccurrences;
        }
    }
}
