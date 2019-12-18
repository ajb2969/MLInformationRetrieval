package testing;

import query.QueryController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GridSearch {
    static final String TRAINING_QUERIES_BASE_PATH = "queries/training";
    static final String TRAINING_RESULTS_PATH = TRAINING_QUERIES_BASE_PATH + "/results";
    private static final String TREC_EVAL_PATH = "./queries/trec_eval";
    private static final int MU_START = 100;
    private static final int MU_DIFFERENCE = 200;
    private static final int MU_ITERATIONS = 20;
    private static final boolean RESULTS_EXIST = true;

    private TrainingRunner trainingRunner;

    public GridSearch(TrainingRunner trainingRunner) {
        this.trainingRunner = trainingRunner;
    }

    public static void main(String[] args) {
        new GridSearch(new TrainingRunner(QueryController.ModelTypes.LDA)).runGridSearch();
    }

    private void runGridSearch() {
        double[] ndcgValues = new double[MU_ITERATIONS];

        int bestNdcgIndex = -1;
        double bestNdcg = -1;
        for (int iter = 0; iter < MU_ITERATIONS; iter++) {
            double mu = MU_START + (iter * MU_DIFFERENCE);
            
            double ndcg;
			try {
				ndcg = RESULTS_EXIST ? runTrecEval(iter) : getNDCG(mu, iter);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
            ndcgValues[iter] = ndcg;

            if (bestNdcg < ndcg) {
                bestNdcg = ndcg;
                bestNdcgIndex = iter;
            }
        }

        double bestMu = MU_START + (bestNdcgIndex * MU_DIFFERENCE);
        System.out.println("Best NDCG : " + String.valueOf(bestNdcg) +
                           ", Best Mu : " + String.valueOf(bestMu));

        for (int iter = 0; iter < MU_ITERATIONS; iter++) {
            double mu = MU_START + (iter * MU_DIFFERENCE);
            System.out.println("Mu=" + String.valueOf(mu) + " NDCG=" + String.valueOf(ndcgValues[iter]));
        }
    }
    

    private double getNDCG(double mu, int iter) {
        System.out.println("Running training queries for iteration " + String.valueOf(iter) +
                           " and mu " + String.valueOf(mu));

        this.trainingRunner.setMu(mu);
        try {
            this.trainingRunner.runTrainingQueries(iter);
            return runTrecEval(iter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double runTrecEval(int iter) throws IOException {
        System.out.println("\tRunning trec_eval on iteration " + String.valueOf(iter));

        String resultsPath = TRAINING_RESULTS_PATH + "/" + String.valueOf(iter) + ".results";
        String qrelsPath = TRAINING_QUERIES_BASE_PATH + "/training.qrels";

        Runtime cl = Runtime.getRuntime();
        Process trec = cl.exec(TREC_EVAL_PATH + " -m ndcg " + qrelsPath + " " + resultsPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(trec.getInputStream()));
        String output = br.readLine();
        String[] split = output.split("\\s+");

        return Double.valueOf(split[2]);
    }
}
