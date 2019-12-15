package testing;

import query.QueryController;

import java.io.IOException;

public class GridSearch {
    public static void main(String[] args) {
        try {
            new TrainingRunner(QueryController.ModelTypes.BM25).runTrainingQueries();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
