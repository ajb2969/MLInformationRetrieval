package retrieval;
import java.util.ArrayList;

import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.BisectingKMeansModel;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class LDA extends Models {

    public LDA() {
        super();
    }

    public ArrayList<Similarity> retrieve(String query) {
        
        return null;
    }

}