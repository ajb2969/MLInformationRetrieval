package retrieval;

import com.google.common.io.Files;
import indexer.Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

abstract public class Models {
    static final int RESULT_SET_SIZE = 30;
    public static final HashMap<String, String> HTMLDOCPATHS = parseDocNumMap();
    private static final String INDICIES_PATH = Index.TOKEN_INDEX_PATH;
    private static final String FILE_TERM_SIZE_PATH = Index.DOCUMENT_SIZE_PATH;
    private static final String DOCNUMMAPPATH = "indicies/doc-num-map.tsv";
    private static final HashMap<String, Integer> FILE_TERM_SIZE = parseDocumentIndexFile(FILE_TERM_SIZE_PATH);
    private static final HashMap<String, Entry> TOKEN_TO_ENTRY = parseTokenIndexFile();
    private static final Map<String, Map<String, Integer>> TERM_TO_FILE_AND_OCCURRENCE = collapseIndexIntoMaps();

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

    static boolean occursInDocuments(String term) {
        return TOKEN_TO_ENTRY.containsKey(term);
    }

    int getOccurrencesInFile(String term, String filename) {
        return TERM_TO_FILE_AND_OCCURRENCE.getOrDefault(term, new HashMap<>()).getOrDefault(filename, 0);
    }

    static String[] extractTerms(String query) {
        return query.toLowerCase().split("\\s+");
    }

    private static HashMap<String, Integer> parseDocumentIndexFile(String path) {
        System.out.println("Parsing document index file");
        HashMap<String, Integer> docMap = new HashMap<>();
        File index = new File(path);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(index));
            String line;
            int counter = 0;
            while((line = reader.readLine()) != null) {
                if(counter < 3) {
                    counter += 1;
                } else {
                    String [] entry = line.trim().split("\t");
                    docMap.put(entry[0], Integer.parseInt(entry[1]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return docMap;
    }

    private static HashMap<String, Entry> parseTokenIndexFile() {
        System.out.println("Parsing token index file");
        try {
            HashMap<String, Entry> terms = new HashMap<>();
            File index = new File(INDICIES_PATH);
            BufferedReader br = new BufferedReader(new FileReader(index));
            String line;
            int counter = 0;

            while ((line = br.readLine()) != null) {
                if(counter < 3) {
                    counter += 1;
                } else {
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
            Files.readLines(f, Charset.defaultCharset()).subList(3, Files.readLines(f, Charset.defaultCharset()).size()).forEach(line -> {
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

    private static double termFrequency(String term, String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences = getTokenToEntryIndex().get(term).getFileOccurrences().stream()
            .filter(e -> e.getFilename().equals(document)).findFirst().orElse(new FileOccurrence("", 0))
            .getOccurrences();

        return (double) occurrences / totalTerms;
    }

    private static double inverseDocumentFrequency(String term) {
        int totalDocuments = getFileTermSize().keySet().size();
        int documentsWithTerm = getTokenToEntryIndex().get(term).getSize();
        return Math.log((double) totalDocuments / documentsWithTerm);
    }

    static class Entry {
        private int size;
        private ArrayList<FileOccurrence> fileOccurrences;

        Entry(int size, ArrayList<FileOccurrence> fileOccurrences) {
            this.size = size;
            this.fileOccurrences = fileOccurrences;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        ArrayList<FileOccurrence> getFileOccurrences() {
            return fileOccurrences;
        }
    }
}
