package query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.io.Files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import indexer.Index;
import retrieval.FileOccurrence;
import retrieval.Models;

@SpringBootApplication
public class Application {

    final static String INDICIES_PATH = Index.TOKEN_INDEX_PATH;
    final static String FILE_TERM_SIZE_PATH = Index.DOCUMENT_SIZE_PATH;
    final static String DOCNUMMAPPATH = "indicies/doc-num-map.tsv";
    private static HashMap<String, String> HTMLDOCPATHS;
    private static HashMap<String, Integer> FILE_TERM_SIZE;
    private static HashMap<String, Models.Entry> TOKEN_TO_ENTRY;
    private static Map<String, Map<String, Integer>> TERM_TO_FILE_AND_OCCURRENCE;
    private static Map<String, Integer> DOCUMENT_SIZES;

    public static void main(String[] args) {
        initialize();
        SpringApplication.run(Application.class, args);
    }



    public  static void initialize() {
        HTMLDOCPATHS = parseDocNumMap();
        FILE_TERM_SIZE = parseDocumentIndexFile(FILE_TERM_SIZE_PATH);
        TOKEN_TO_ENTRY = parseTokenIndexFile();
        TERM_TO_FILE_AND_OCCURRENCE = collapseIndexIntoMaps();
        DOCUMENT_SIZES = getSizeIndex();
    }


    /**
     * @return the dOCUMENT_SIZES
     */
    public static Map<String, Integer> getDOCUMENT_SIZES() {
        return DOCUMENT_SIZES;
    }


    /**
     * @return the tERM_TO_FILE_AND_OCCURRENCE
     */
    public static Map<String, Map<String, Integer>> getTERM_TO_FILE_AND_OCCURRENCE() {
        return TERM_TO_FILE_AND_OCCURRENCE;
    }


    /**
     * @return the tOKEN_TO_ENTRY
     */
    public static HashMap<String, Models.Entry> getTOKEN_TO_ENTRY() {
        return TOKEN_TO_ENTRY;
    }




    /**
     * @return the fILE_TERM_SIZE
     */
    public static HashMap<String, Integer> getFILE_TERM_SIZE() {
        return FILE_TERM_SIZE;
    }



    /**
     * @return the hTMLDOCPATHS
     */
    public static HashMap<String, String> getHTMLDOCPATHS() {
        return HTMLDOCPATHS;
    }





    private static HashMap<String, String> parseDocNumMap() {
        System.out.println("Parsing doc num map");
        HashMap<String, String> siMap = new HashMap<>();
        File f = new File(DOCNUMMAPPATH);
        try {
            Files.readLines(f, Charset.defaultCharset()).subList(0, Files.readLines(f, Charset.defaultCharset()).size())
                    .forEach(line -> {
                        String[] tokens = line.split("\t");
                        siMap.put(tokens[0].trim(), tokens[1].trim());
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return siMap;
    }

    private static HashMap<String, Integer> parseDocumentIndexFile(String path) {
        System.out.println("Parsing document index file");
        HashMap<String, Integer> docMap = new HashMap<>();
        File index = new File(path);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(index));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entry = line.trim().split("\t");
                docMap.put(entry[0], Integer.parseInt(entry[1]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return docMap;
    }

    private static HashMap<String, Integer> getSizeIndex() {
        File inputFile = new File(Index.DOCUMENT_SIZE_PATH);
        HashMap<String, Integer> documentSizes = new HashMap<>();
        try {
            BufferedReader bw = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = bw.readLine()) != null) {
                String[] file = line.split("\t");
                documentSizes.put(file[0], Integer.parseInt(file[1]));
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentSizes;
    }

    public static HashMap<String, Models.Entry> parseTokenIndexFile() {
        System.out.println("Parsing token index file");
        try {
            HashMap<String, Models.Entry> terms = new HashMap<>();
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
                terms.put(term, new Models.Entry(quantity, documents));
            }
            br.close();
            return terms;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static Map<String, Map<String, Integer>> collapseIndexIntoMaps() {
        return TOKEN_TO_ENTRY.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> convertToMap(entry.getValue())));
    }

    private static Map<String, Integer> convertToMap(Models.Entry indexEntry) {
        return indexEntry.getFileOccurrences().stream()
            .collect(Collectors.toMap(
                FileOccurrence::getFilename,
                FileOccurrence::getOccurrences));
    }


}


