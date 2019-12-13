package clustering;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.ejml.data.DMatrixSparseCSC;

import retrieval.LDA;


class SparseMatrix {
    private DMatrixSparseCSC csc;
    private ConcurrentHashMap<String, List<LDA.Index>> cscMatrix;
    SparseMatrix(ConcurrentHashMap<String, List<LDA.Index>> cscMatrix, int rows) {
        List<String> documents = new ArrayList<>(cscMatrix.keySet());
        Collections.sort(documents);
        this.cscMatrix = cscMatrix;
        this.csc = new DMatrixSparseCSC(rows, documents.size());


        for(int document = 0; document < documents.size(); document++) {
            for(LDA.Index value: this.cscMatrix.get(documents.get(document))) {
                this.csc.set(value.getIndex(), document, value.getValue());
            }
        }
    }


    /**
     * @return the csc
     */
    public synchronized DMatrixSparseCSC getCsc() {
        return csc;
    }
}