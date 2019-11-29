package indexer;

import com.google.common.collect.Maps;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Index {
    public static String docs_file_path = "documents/";
    public static String output_dir = "indicies/doc-index.tsv";
    public static String docSize = "indicies/doc-size-index.tsv";
    public static String tokenPositionsFile = "indicies/token-pos.tsv";
    public static final String SEASON_INDEX_PATH = "indicies/season-index.tsv";

    private static void document_level() {
        // Map of Filename -> <word, occurrences in file>
        HashMap<String, Map<String, Integer>> doc_index = new HashMap<>();
        // Map of Token -> File -> [positions in which the word occurs]
        HashMap<String, Integer> totalTokens = new HashMap<>();
        HashMap<String, HashMap<String, ArrayList<Integer>>> tokenPositions =
                new HashMap<>();
        HashMap<String, Integer> titleToSeason = new HashMap<>();
        File[] files = new File(docs_file_path).listFiles();
        //brackets, parenthesis, colons, periods, commas, html tags, split on
        // spaces, remove new-line
        for (File f : files) {
            try {
                AtomicInteger sum = new AtomicInteger();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                Map<String, Integer> wordOccurrences = Maps.newHashMap();
                int metaDataCounter = 0;
                String title = "";
                int season = 0;
                while ((line = br.readLine()) != null) {
                    if (metaDataCounter == 1) {
                        // season
                        season = Integer.parseInt(line.split(" ")[1]);
                    }
                    //String [] split = line.split("[.\\[\\]!.:\"?,\s]");

                    line = line.replaceAll("<[^>]*>", "").replaceAll("\\p" +
                            "{Punct}", "");
                    String[] split = line.split(" ");
                    Arrays.stream(split)
                            .filter(token -> !token.isEmpty() || !token.equals(""))
                            .map(token -> token.replaceAll("[.,!?:\\[\\]]", ""))
                            .map(String::toLowerCase)
                            .forEach(token -> {
                                if (tokenPositions.containsKey(token)) {
                                    HashMap<String, ArrayList<Integer>> temp
                                            = tokenPositions.get(token);
                                    if (temp.containsKey(f.getName())) {
                                        ArrayList<Integer> positions =
                                                temp.get(f.getName());
                                        positions.add(sum.intValue());
                                        temp.put(f.getName(), positions);
                                        tokenPositions.put(token, temp);
                                    } else {
                                        ArrayList<Integer> positions =
                                                new ArrayList<>();
                                        positions.add(sum.intValue());
                                        temp.put(f.getName(), positions);
                                        tokenPositions.put(token, temp);
                                    }
                                } else {
                                    HashMap<String, ArrayList<Integer>> temp
                                            = new HashMap<>();
                                    ArrayList<Integer> tempPos =
                                            new ArrayList<>();
                                    tempPos.add(sum.intValue());
                                    temp.put(f.getName(), tempPos);
                                    tokenPositions.put(token, temp);
                                }
                                sum.addAndGet(1);
                                addOccurrence(token, wordOccurrences);
                            });

                    metaDataCounter++;
                }
                doc_index.put(f.getName(), wordOccurrences);
                totalTokens.put(f.getName(), sum.intValue());
                titleToSeason.put(f.getName(), season);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //map of token -> files it exists in
        HashMap<String, ArrayList<String>> index = new HashMap<>();
        for (String file_name : doc_index.keySet()) {
            for (Entry<String, Integer> wordOccurrenceEntry :
                    doc_index.get(file_name).entrySet()) {
                String token = wordOccurrenceEntry.getKey();
                String filenameWithOccurrences =
                        file_name + ":" + String.valueOf(wordOccurrenceEntry.getValue());
                if (!token.equals("")) {
                    if (index.containsKey(token) && !index.get(token).contains(file_name)) {
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
            write_index(index);
            writeSizeIndex(totalTokens);
            writePositionIndex(tokenPositions);
            writeEpisodeIndex(totalTokens, docSize);
            writeEpisodeIndex(titleToSeason, SEASON_INDEX_PATH);
        } catch (IOException e) {
            System.err.println("Unable to create index file");
        }
    }

    private static void addOccurrence(String word,
                                      Map<String, Integer> wordOccurrences) {
        if (wordOccurrences.containsKey(word)) {
            Integer occurrences = wordOccurrences.get(word);
            wordOccurrences.put(word, occurrences + 1);
        } else {
            wordOccurrences.put(word, 1);
        }
    }

    private static void write_index(HashMap<String, ArrayList<String>> index) throws IOException {
        File output_file = new File(output_dir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String term : index.keySet()) {
            bw.write(term + "\t" + index.get(term).size() + "\t");
            for (String file : index.get(term)) {
                bw.write(file + "\t");
            }
            bw.write("\n");
            bw.flush();
        }
    }

    private static void writeEpisodeIndex(HashMap<String, Integer> index, String path) throws IOException {
        File output_file = new File(path);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String file : index.keySet()) {
            bw.write(file + "\t" + index.get(file));
            bw.write("\n");
            bw.flush();
        }
    }

    private static void writeSizeIndex(HashMap<String, Integer> index) throws IOException {
        File output_file = new File(docSize);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String file : index.keySet()) {
            bw.write(file + "\t" + index.get(file));
            bw.write("\n");
            bw.flush();
        }
    }

    private static void writePositionIndex(HashMap<String, HashMap<String,
            ArrayList<Integer>>> tokenPositions) throws IOException {
        File outputFile = new File(tokenPositionsFile);
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
    }

    public static ArrayList<Integer> readQueryPositions(ArrayList<String> terms, String docName) throws IOException {

        File inputFile = new File(tokenPositionsFile);
        List<String> lines = Files.readAllLines(Paths.get(inputFile.getCanonicalPath()));
        ArrayList<String> selectedLines = new ArrayList<>();
        for(String line: lines) {
            String token = line.split("\t")[0];
            if(terms.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(token)) {
                selectedLines.add(line);
            }
        }
        ArrayList<Integer> positions = new ArrayList<>();
        for(String selected: selectedLines) {
            String token = selected.split("\t")[0];
            String[] filePositionsLine = selected.split("\t");
            for(int i = 1; i < filePositionsLine.length; i++) {
                if (filePositionsLine[i].split(":")[0].equals(docName)) {
                    for(String position: filePositionsLine[i].split(":")[1].split(",")) {
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
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        public String getFile() {
            return file;
        }

        public ArrayList<Integer> getPositions() {
            return positions;
        }
    }


    public static void main(String[] args) {
        document_level();
    }
}
