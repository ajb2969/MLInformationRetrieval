package indexer;

import com.google.common.collect.Maps;

import java.io.*;
import java.io.File;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Index {
    public static String DOC_DIRECTORY = "documents/";
    public static String TOKEN_INDEX_PATH = "indicies/doc-index.tsv";
    public static String DOCUMENT_SIZE_PATH = "indicies/doc-size-index.tsv";
    public static String TOKEN_POSITION_PATH = "indicies/token-pos.tsv";

    public static void main(String[] args) {
        File[] files = new File(DOC_DIRECTORY).listFiles();
        createIndices(files);
    }

    private static void createIndices(File[] files){
        createDocumentSizeIndex(files);
        createTokenIndex(files);
        createTokenPositionIndex(files);
    }

    private static void createTokenIndex(File[] files) {
        System.out.println("Creating Token index file");
        // Map of Filename -> <word, occurrences in file>
        HashMap<String, Map<String, Integer>> fileToWordAndOccurrences = new HashMap<>();
        for (File f : files) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                Map<String, Integer> wordOccurrences = Maps.newHashMap();
                while ((line = br.readLine()) != null) {
                    // String [] split = line.split("[.\\[\\]!.:\"?,\s]");

                    line = line.replaceAll("<[^>]*>", "").replaceAll("\\p" + "{Punct}", "");
                    String[] split = line.split(" ");
                    Arrays.stream(split)
                        .filter(token -> !token.isEmpty())
                        .map(token -> token.replaceAll("[.,!?:\\[\\]]", ""))
                        .map(String::toLowerCase)
                        .forEach(token -> addOccurrence(token, wordOccurrences));
                }
                br.close();
                fileToWordAndOccurrences.put(f.getName(), wordOccurrences);
            } catch (IOException e) {
                System.err.println("Error extracting word occurrences from file " + f.getName());
                throw new RuntimeException(e);
            }
        }

        // map of token -> files it exists in
        HashMap<String, ArrayList<String>> index = new HashMap<>();
        for (String filename : fileToWordAndOccurrences.keySet()) {
            for (Entry<String, Integer> wordOccurrenceEntry : fileToWordAndOccurrences.get(filename).entrySet()) {
                String token = wordOccurrenceEntry.getKey();
                String filenameWithOccurrences = filename + ":" + String.valueOf(wordOccurrenceEntry.getValue());
                if (!token.equals("")) {
                    if (index.containsKey(token) && !index.get(token).contains(filename)) {
                        ArrayList<String> documents = index.get(token);
                        documents.add(filenameWithOccurrences);
                        index.put(token, documents);
                    } else {
                        ArrayList<String> documents = new ArrayList<>();
                        documents.add(filenameWithOccurrences);
                        index.put(token, documents);
                    }
                }
            }
        }

        try {
            writeTokenIndex(index);
        } catch (IOException e) {
            System.err.println("Unable to create Token index file");
            throw new RuntimeException(e);
        }
    }

    private static void createTokenPositionIndex(File[] files) {
        System.out.println("Creating Token Position index file");
        // Map of Token -> File -> [positions in which the word occurs]
        HashMap<String, HashMap<String, ArrayList<Integer>>> tokenPositions = new HashMap<>();
        for (File f : files) {
            try {
                AtomicInteger sum = new AtomicInteger();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    // String [] split = line.split("[.\\[\\]!.:\"?,\s]");

                    line = line.replaceAll("<[^>]*>", "").replaceAll("\\p" + "{Punct}", "");
                    String[] split = line.split(" ");
                    Arrays.stream(split)
                        .filter(token -> !token.isEmpty())
                        .map(token -> token.replaceAll("[.,!?:\\[\\]]", ""))
                        .map(String::toLowerCase)
                        .forEach(token -> {
                            if (tokenPositions.containsKey(token)) {
                                HashMap<String, ArrayList<Integer>> temp = tokenPositions.get(token);
                                if (temp.containsKey(f.getName())) {
                                    ArrayList<Integer> positions = temp.get(f.getName());
                                    positions.add(sum.intValue());
                                    temp.put(f.getName(), positions);
                                    tokenPositions.put(token, temp);
                                } else {
                                    ArrayList<Integer> positions = new ArrayList<>();
                                    positions.add(sum.intValue());
                                    temp.put(f.getName(), positions);
                                    tokenPositions.put(token, temp);
                                }
                            } else {
                                HashMap<String, ArrayList<Integer>> temp = new HashMap<>();
                                ArrayList<Integer> tempPos = new ArrayList<>();
                                tempPos.add(sum.intValue());
                                temp.put(f.getName(), tempPos);
                                tokenPositions.put(token, temp);
                            }
                            sum.addAndGet(1);
                        });
                }
                br.close();
            } catch (IOException e) {
                System.err.println("Error extracting token positions from file " + f.getName());
                throw new RuntimeException(e);
            }
        }

        try {
            writePositionIndex(tokenPositions);
        } catch (IOException e) {
            System.err.println("Unable to create Token Positions index");
            throw new RuntimeException(e);
        }

    }

    private static void createDocumentSizeIndex(File[] files) {
        System.out.println("Creating Document Size index file");
        HashMap<String, Integer> documentToSize = new HashMap<>();
        try {
            for (File f : files) {
                int sum = 0;
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.replaceAll("<[^>]*>", "").replaceAll("\\p" + "{Punct}", "");
                    sum += Arrays.stream(line.split(" "))
                        .filter(token -> !token.isEmpty())
                        .count();
                }
                br.close();
                documentToSize.put(f.getName(), sum);
            }
            writeSizeIndex(documentToSize);
        } catch (IOException e) {
            System.err.println("Unable to create Document Size index file");
            throw new RuntimeException(e);
        }
    }

    private static void addOccurrence(String word, Map<String, Integer> wordOccurrences) {
        if (wordOccurrences.containsKey(word)) {
            Integer occurrences = wordOccurrences.get(word);
            wordOccurrences.put(word, occurrences + 1);
        } else {
            wordOccurrences.put(word, 1);
        }
    }

    private static void writeTokenIndex(HashMap<String, ArrayList<String>> index) throws IOException {
        File output_file = new File(TOKEN_INDEX_PATH);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String term : index.keySet()) {
            bw.write(term + "\t" + index.get(term).size() + "\t");
            for (String file : index.get(term)) {
                bw.write(file + "\t");
            }
            bw.write("\n");
            bw.flush();
        }
        bw.close();
    }



    private static void writeSizeIndex(HashMap<String, Integer> index) throws IOException {
        File output_file = new File(DOCUMENT_SIZE_PATH);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String file : index.keySet()) {
            bw.write(file + "\t" + index.get(file));
            bw.write("\n");
            bw.flush();
        }
        bw.close();
    }

    private static void writePositionIndex(HashMap<String, HashMap<String, ArrayList<Integer>>> tokenPositions)
            throws IOException {
        File outputFile = new File(TOKEN_POSITION_PATH);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for (String token : tokenPositions.keySet()) {
            bw.write(token + "\t");
            for (String file : tokenPositions.get(token).keySet()) {
                bw.write(file + ":");
                for (int pos = 0; pos < tokenPositions.get(token).get(file).size(); pos++) {
                    if (pos == tokenPositions.get(token).get(file).size() - 1) {
                        bw.write(tokenPositions.get(token).get(file).get(pos) + "\t");
                    } else {
                        bw.write(tokenPositions.get(token).get(file).get(pos) + ",");
                    }
                }
            }
            bw.write("\n");
        }
        bw.close();
    }

    public static ArrayList<Integer> readQueryPositions(ArrayList<String> terms, String docName) throws IOException {

        File inputFile = new File(TOKEN_POSITION_PATH);
        
        BufferedReader reader = new BufferedReader(new FileReader(new File(Paths.get(inputFile.getCanonicalPath()).toString())));
        ArrayList<String> selectedLines = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null) {
            String token = line.split("\t")[0];
            if (terms.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(token)) {
                selectedLines.add(line);
            }
        }
        reader.close();
        ArrayList<Integer> positions = new ArrayList<>();
        for (String selected : selectedLines) {
            String[] filePositionsLine = selected.split("\t");
            for (int i = 1; i < filePositionsLine.length; i++) {
                if (filePositionsLine[i].split(":")[0].equals(docName)) {
                    for (String position : filePositionsLine[i].split(":")[1].split(",")) {
                        positions.add(Integer.parseInt(position));
                    }
                }
            }

        }
        return positions;
    }

    public static class Positions {
        private String file;
        private ArrayList<Integer> positions;

        Positions(String filePositions) {
            this.file = filePositions.split(":")[0];
            positions = (ArrayList<Integer>) Arrays.stream(filePositions.split(":")[1].split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
        }

        public String getFile() {
            return file;
        }

        public ArrayList<Integer> getPositions() {
            return positions;
        }
    }
}
