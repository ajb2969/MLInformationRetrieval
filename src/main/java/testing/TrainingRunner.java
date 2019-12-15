package testing;

import query.QueryController;
import retrieval.Pooling;
import retrieval.Similarity;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainingRunner {
    private static final int RESULT_COUNT = 10;

    private final Pooling pooling;

    public TrainingRunner(QueryController.ModelTypes model) {
        this.pooling = new Pooling("", model);
    }

    public void setMu(double mu) {
        this.pooling.setMu(mu);
    }

    public void runTrainingQueries(int iter) throws IOException {
        List<String> trecResults = new ArrayList<>();
        File[] groupDirectories = new File(GridSearch.TRAINING_QUERIES_BASE_PATH).listFiles();
        for (File group : groupDirectories) {
            if (!group.getName().equals("results") && !group.getName().equals("training.qrels")) {
                List<String> groupTrecResults = runGroupQueries(group);
                trecResults.addAll(groupTrecResults);
            }
        }
        writeToResultsFile(String.join("\n", trecResults), iter);
    }

    private List<String> runGroupQueries(File groupDirectory) throws IOException {
        String groupNumber = groupDirectory.getName();
        System.out.println("Running queries for Group " + groupNumber);

        // get queries
        File queriesFile = new File(GridSearch.TRAINING_QUERIES_BASE_PATH + "/" + groupNumber + "/queries.txt");
        BufferedReader queriesBr = new BufferedReader(new FileReader(queriesFile));
        ArrayList<String> queries = new ArrayList<>();
        String line;
        while ((line = queriesBr.readLine()) != null) {
            line = line.trim();
            queries.add(line);
        }
        queriesBr.close();

        List<String> trecResultLines = IntStream
            .range(0, queries.size())
            .mapToObj(queryIndex -> convertToTrecResults(queries.get(queryIndex), groupNumber, queryIndex + 1))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        return trecResultLines;
    }

    private List<String> convertToTrecResults(String query, String groupNumber, int queryNum) {
        String queryNumber = String.valueOf(queryNum);
        this.pooling.setQuery(query);
        ArrayList<Similarity> results = this.pooling.retrieve();

        System.out.println(query + " : " + queryNumber);

        List<String> trecResults = IntStream
            .range(0, RESULT_COUNT)
            .mapToObj(resultIndex -> convertToTrecResult(results.get(resultIndex), groupNumber, queryNumber, RESULT_COUNT - resultIndex))
            .collect(Collectors.toList());
        return trecResults;
    }

    private String convertToTrecResult(Similarity document, String groupNum, String queryNum, int score) {
        return groupNum + queryNum + " Q0 " + document.getDocumentName() + " 0 " + String.valueOf(score) + " STANDARD";
    }

    private void writeToResultsFile(String trecResults, int iter) throws IOException {
        File resultsFile = new File(GridSearch.TRAINING_RESULTS_PATH + "/" + String.valueOf(iter) + ".results");
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile));
        bw.write(trecResults);
        bw.flush();
        bw.close();
    }
}
