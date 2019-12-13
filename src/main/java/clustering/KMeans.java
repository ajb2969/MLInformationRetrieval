package clustering;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import query.Application;
import retrieval.FileOccurrence;
import retrieval.LDA;
import retrieval.Models;
import retrieval.LDA.Index;

public class KMeans {
    static HashMap<String, Models.Entry> TOKEN_TO_ENTRY;
    static Map<String, Map<String, Integer>> TERM_TO_FILE_AND_OCCURRENCE;

    private static void initialize() {
        TOKEN_TO_ENTRY = Application.parseTokenIndexFile();
        TERM_TO_FILE_AND_OCCURRENCE = collapseIndexIntoMaps();
    }

    public static Map<String, Map<String, Integer>> collapseIndexIntoMaps() {
        return TOKEN_TO_ENTRY.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertToMap(entry.getValue())));
    }

    private static Map<String, Integer> convertToMap(Models.Entry indexEntry) {
        return indexEntry.getFileOccurrences().stream()
                .collect(Collectors.toMap(FileOccurrence::getFilename, FileOccurrence::getOccurrences));
    }

    public static ConcurrentHashMap<String, List<Index>> readSparseMatrix(String outputFile) {
        ConcurrentHashMap<String, List<Index>> parsedDocs = new ConcurrentHashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(outputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entries = line.split("\t");
                String key = entries[0];
                ArrayList<LDA.Index> values = new ArrayList<>();
                for (int index = 1; index < entries.length; index++) {
                    String[] column = entries[index].split(",");
                    values.add(new LDA.Index(Integer.parseInt(column[0]), Double.parseDouble(column[1])));
                }
                parsedDocs.put(key, values);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsedDocs;
    }

    private static double klDivergence(List<Double> clusterCenter, List<SimpleEntry> currentDocument) {
        // TODO compute KL Divergence
        double dirchletPriorWeight = 0.001;
        // TODO for each value, add dirchlet prior weight to it, removes 0 values and scales everything by small value
        return 0.0;
    }

    private static HashMap<Integer, List<Integer>> clustering(SparseMatrix sm, int dim, int k) {
        /**
         * Design decisions: Cluster centers: medoids Stopping Decision: When cluster
         * centers have not moved for 10 iterations Distance Metric: Relative-Entropy:
         * KL-Divergence
         */

        ConcurrentHashMap<List<Double>, List<Integer>> clusterCenters = new ConcurrentHashMap<>();

        System.out.println("Building cluster centers");
        for (int center = 0; center < k; center++) {
            Random rand = new Random(31 + center);
            clusterCenters.put((List<Double>) rand.doubles().filter(e -> Double.valueOf(e) != 0).limit(dim).boxed()
                    .collect(Collectors.toList()), new ArrayList<>());
        }

        boolean change;
        System.out.println("Running clustering");
        do {
            // while there are changes to data points
            change = false;
            System.out.println("Building proper histogram for each document");
            IntStream.range(0, sm.getCsc().numCols).boxed().forEach(document -> {
                final int documentColumn = document;

                List<SimpleEntry> documentHistogram = (List<SimpleEntry>) IntStream
                                                .range(0, sm.getCsc().numRows)
                                                .filter(index -> sm.getCsc().get(index, documentColumn) != 0)
                                                .mapToObj(index -> new SimpleEntry(index, (sm.getCsc().get(index, documentColumn))))
                                                .collect(Collectors.toList());


                for (List<Double> center : clusterCenters.keySet()) {
                    //compute closest cluster center to document(columns)
                    //if change then set change to true
                    double distance = klDivergence(center, documentHistogram);
                }
            });

            //recompute cluster centers based upon 
        } while(change != false);

        return null;
    }




    public static void main(String [] args) {
        System.out.println("Initializing Indicies");
        initialize();
        System.out.println("Reading in and building Sparse Matrix");
        ConcurrentHashMap<String, List<Index>> parsedDocs = readSparseMatrix(LDA.vecModelFilePath);
        SparseMatrix sm = new SparseMatrix(parsedDocs, TOKEN_TO_ENTRY.keySet().size());
        System.out.println("Starting clustering");
        for(int k = 10; k < 40; k+=10) {
            System.out.println("Building cluster centers for k=" + k);
            HashMap<Integer, List<Integer>> clusterDecisions = clustering(sm, TOKEN_TO_ENTRY.keySet().size(), k);
        }

        /*
        K - number of topics a document belongs to
        V - size of the vocabulaty
        M - number of documents
        N - number of words in each document
        w - a word in a document
        *w* - a document
        D - a collection of M documents
        z - a topic of the k topics, 1/k probability
        theta - a matrix where (i, j) is the probability of document i containing words related to topic j
        // use kmeans to minimize kl divergence between each center and document

        */
        
        System.out.println("Finished clustering");
    }


}