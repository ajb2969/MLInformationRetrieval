package retrieval;

import com.google.common.io.Files;
import indexer.Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

abstract public class Models {
    private static final String indicies_path = Index.output_dir;
    private static final String FILE_TERM_SIZE_PATH = Index.docSize;
    static final HashMap<String, Integer> fileTermSize = parseDocumentIndexFile(FILE_TERM_SIZE_PATH);
    static final HashMap<String, Entry> documents = parse_doc_indicies();
    public static final HashMap<String, Integer> DOCS_TO_SEASONS = parseDocumentIndexFile(Index.SEASON_INDEX_PATH);
    static final int DOCUMENTSRETURNED = 30;
    static final Map<String, Map<String, Integer>> termToFileAndOccurrence = createIndexMap();

    Models() {
    }

    private static HashMap<String, Integer> parseDocumentIndexFile(String path) {
        HashMap<String, Integer> docMap = new HashMap<>();
        File index = new File(path);

        try {
            Files.readLines(index, Charset.defaultCharset())
                .forEach(entry -> docMap.put(entry.split("\t")[0], Integer.parseInt(entry.split("\t")[1])));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return docMap;
    }

    ArrayList<String> getDocumentList() {
        ArrayList<String> documents = new ArrayList<>();
        for (File f :
                Objects.requireNonNull(new File(Index.docs_file_path).listFiles())) {
            documents.add(f.getName());
        }
        return documents;
    }

    public static HashMap<String, Entry> get_doc_indicies() {
        return documents;
    }

    public static boolean occursInDocuments(String term) {
        return get_doc_indicies().containsKey(term);
    }

    public abstract ArrayList<Similarity> retrieve(String query);

    String[] extractTerms(String query) {
        return query.toLowerCase().split("\\s+");
    }


    private static HashMap<String, Entry> parse_doc_indicies() {
        try {
            HashMap<String, Entry> terms = new HashMap<>();
            File index = new File(indicies_path);
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
                        documents.add(new FileOccurrence(fileAndOccurrence[0] +
                            ":" + fileAndOccurrence[1],
                            Integer.valueOf(fileAndOccurrence[2])));
                    } else {
                        documents.add(new FileOccurrence(fileAndOccurrence[0],
                            Integer.valueOf(fileAndOccurrence[1])));
                    }
                }
                terms.put(term, new Entry(quantity, documents));
            }

            return terms;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static Map<String, Map<String, Integer>> createIndexMap() {
        return documents.entrySet().stream()
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

    public static HashMap<String, Integer> getFileTermSize() {
        return fileTermSize;
    }

    public int getOccurrencesInFile(String term, String filename) {
        return termToFileAndOccurrence.getOrDefault(term, new HashMap<>()).getOrDefault(filename, 0);
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

        public ArrayList<FileOccurrence> getFileOccurrences() {
            return fileOccurrences;
        }

        public void setFileOccurrences(ArrayList<FileOccurrence> fileOccurrences) {
            this.fileOccurrences = fileOccurrences;
        }
    }
}
