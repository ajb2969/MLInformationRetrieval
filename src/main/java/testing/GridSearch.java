package testing;

import query.QueryController;

import java.io.IOException;

public class GridSearch {
    static final String TRAINING_QUERIES_BASE_PATH = "queries/training";
    private static final String TREC_EVAL_PATH = "queries/trec_eval";
    private static final int MU_START = 100;
    private static final int MU_DIFFERENCE = 5;
    private static final int MU_ITERATIONS = 5;

    private TrainingRunner trainingRunner;

    public GridSearch(TrainingRunner trainingRunner) {
        this.trainingRunner = trainingRunner;
    }

    public static void main(String[] args) {
        try {
            new TrainingRunner(QueryController.ModelTypes.BM25).runTrainingQueries();

            if (false) {
                new GridSearch(new TrainingRunner(QueryController.ModelTypes.LDA)).runGridSearch();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runGridSearch() {
        double[] ndcgValues = new double[MU_ITERATIONS];

        for (int iter = 0; iter < MU_ITERATIONS; iter++) {
            int mu = MU_START + (iter * MU_DIFFERENCE);
            ndcgValues[iter] = getNDCG(mu);
        }
    }

    private double getNDCG(int mu) {
        this.trainingRunner.setMu(mu);
        try {
            this.trainingRunner.runTrainingQueries();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return runTrecEval(mu);
    }

    private double runTrecEval(int mu) {


        return 0.0;
    }
}
