package retrieval;

import query.QueryController;

import java.util.ArrayList;

import static retrieval.Models.RESULT_SET_SIZE;

public class Pooling {
    private Models bm25;
    private Models lda;
    private String query;
    private QueryController.ModelTypes currModel;

    public Pooling(String query, QueryController.ModelTypes model) {
        this.bm25 = new BM25();
        this.lda = new LDA();
        this.query = query;
        this.currModel = model;
    }


    public ArrayList<Similarity> retrieve() {
        ArrayList<Similarity> ldaResults = this.lda.retrieve(this.query);
        ArrayList<Similarity> bm25Results = this.bm25.retrieve(this.query);
        ArrayList<Similarity> returnedResults = new ArrayList<>();
        System.out.println("Retrieving results for model " + this.currModel.toString());

        if(this.currModel == QueryController.ModelTypes.LDA) {
            for(int i = 0; i < RESULT_SET_SIZE; i++) {
                returnedResults.add(ldaResults.get(i));
            }
        } else if(this.currModel == QueryController.ModelTypes.BM25) {
            for(int i = 0; i < RESULT_SET_SIZE; i++) {
                returnedResults.add(bm25Results.get(i));
            }
        } else {
            for (int i = 0; i < RESULT_SET_SIZE; ) {
                if (i % 2 == 0 && !returnedResults.contains(bm25Results.get(i))) {
                    returnedResults.add(bm25Results.get(i));
                }

                if (i % 2 == 1 && !returnedResults.contains(ldaResults.get(i))) {
                    returnedResults.add(ldaResults.get(i));
                }

                i++;
            }
        }
        return returnedResults;
    }
}
