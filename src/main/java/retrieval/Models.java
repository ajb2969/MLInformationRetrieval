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
    /*
    private static HashMap<String, Integer> parseDocumentIndexFile(String path) {
        System.out.println("Parsing document index file");
        HashMap<String, Integer> docMap = new HashMap<>();
        File index = new File(path);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(index));
            String line;
            while((line = reader.readLine()) != null) {
                String [] entry = line.trim().split("\t");
                docMap.put(entry[0], Integer.parseInt(entry[1]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return docMap;
    }
    

    private static HashMap<String, Integer> getSizeIndex(){
        File inputFile = new File(Index.DOCUMENT_SIZE_PATH);
        HashMap<String, Integer> documentSizes = new HashMap<>();
        try {
            BufferedReader bw = new BufferedReader(new FileReader(inputFile));
            String line;
            while((line = bw.readLine()) != null) {
                String [] file = line.split("\t");
                documentSizes.put(file[0], Integer.parseInt(file[1]));
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return documentSizes;
    }
    

    private static HashMap<String, Entry> parseTokenIndexFile() {
        System.out.println("Parsing token index file");
        try {
            HashMap<String, Entry> terms = new HashMap<>();
            File index = new File(INDICIES_PATH);
            BufferedReader br = new BufferedReader(new FileReader(index));
            String line;

            while ((line = br.readLine()) != null) {
                String[] parsed_line = line.split("\t");
                String term = parsed_line[0];
                int quantity = Integer.parseInt(parsed_line[1]);
                ArrayList<FileOccurrence> documents = new ArrayList<>();
                for (int i = 2; i < parsed_line.length; i++) {
                    String[] fileAndOccurrence = parsed_line[i].split(":");
                    if (fileAndOccurrence.length == 3) {
                        documents.add(new FileOccurrence(fileAndOccurrence[0] + ":" + fileAndOccurrence[1],
                                Integer.valueOf(fileAndOccurrence[2])));
                    } else {
                        documents.add(new FileOccurrence(fileAndOccurrence[0], Integer.valueOf(fileAndOccurrence[1])));
                    }
                }
                terms.put(term, new Entry(quantity, documents));
            }
            br.close();
            return terms;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static HashMap<String, String> parseDocNumMap() {
        System.out.println("Parsing doc num map");
        HashMap<String, String> siMap = new HashMap<>();
        File f = new File(DOCNUMMAPPATH);
        try {
            Files.readLines(f, Charset.defaultCharset()).subList(0, Files.readLines(f, Charset.defaultCharset()).size()).forEach(line -> {
                String[] tokens = line.split("\t");
                siMap.put(tokens[0].trim(), tokens[1].trim());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return siMap;
    }
    

    private static Map<String, Map<String, Integer>> collapseIndexIntoMaps() {
        return TOKEN_TO_ENTRY.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> convertToMap(entry.getValue())));
    }

    private static Map<String, Integer> convertToMap(Entry indexEntry) {
        return indexEntry.getFileOccurrences().stream()
            .collect(Collectors.toMap(
                FileOccurrence::getFilename,
                FileOccurrence::getOccurrences));
    }
    */
    static double termFrequency(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = getTokenToEntryIndex().get(term).getFileOccurrences().stream()
            .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
            .getOccurrences();

        return (double) occurrences / totalTerms;
    }

    static TFSmoothing termFreqencySmoothing(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = getTokenToEntryIndex().get(term).getFileOccurrences().stream()
            .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
            .getOccurrences();

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
