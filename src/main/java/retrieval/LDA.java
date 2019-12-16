package retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.File;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import query.Application;

public class LDA extends Models {
    public final static String vecModelFilePath = "indicies/vecSpaceModel.tsv";

    private double mu = 1900.0;

    public LDA() {
        super();
    }

    public LDA(double mu) {
        super();
        this.mu = mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }
    
    public static class Index {
        private int index;
        private double value;

        public Index(int index, double value) {
            this.index = index;
            this.value = value;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return the value
         */
        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.index + "," + this.value;
        }
    }

    public ArrayList<Similarity> retrieve(String query) {
        System.out.println("Retrieving LDA");
        String[] terms = extractTerms(query);
        ConcurrentHashMap<String, List<Index>> parsedDocs;
        File outputFile = new File(vecModelFilePath);
        List<String> docTokens;
        List<String> documents = new ArrayList<>(Application.getFILE_TERM_SIZE().keySet());
        Collections.sort(documents);
        if(!outputFile.exists()) {
            docTokens = new ArrayList<>(Application.getTOKEN_TO_ENTRY().keySet());
            Collections.sort(docTokens);
            try {
                Scheduler schedule = new Scheduler(documents, docTokens, outputFile);
                parsedDocs = schedule.executeParse();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            System.out.println("Reading in compressed sparse matrix");
            //http://www.jmlr.org/papers/volume3/blei03a/blei03a.pdf
            HashMap<String, TFSmoothing> ranking = new HashMap<>();
            int totalCollectionTokens = Application.getDOCUMENT_SIZES().keySet().stream().mapToInt(key -> Models.getDocumentTokenSizes().get(key)).sum();
            for(String term: terms) {
                int collectionTermsQuantity = occursInDocuments(term)? Application.getTERM_TO_FILE_AND_OCCURRENCE().get(term).values().stream().reduce(0, Integer::sum) : 0;
                for(String document: documents) {
                    //need to add P_LDA
                    if(!ranking.containsKey(document)) {
                        TFSmoothing termTF = Models.termFreqencySmoothing(term, document);
                        termTF.setMu(mu);
                        termTF.setSmoothing(collectionTermsQuantity, totalCollectionTokens);
                        ranking.put(document, termTF);
                    } else {
                        TFSmoothing termTF = Models.termFreqencySmoothing(term, document);
                        termTF.setMu(mu);
                        termTF.setSmoothing(collectionTermsQuantity, totalCollectionTokens);
                        termTF.setScore(ranking.get(document).getScore());
                        ranking.put(document, termTF);
                    }
                }
            }

            return (ArrayList<Similarity>) new ArrayList<>(ranking.keySet()).stream()
                    .sorted((document1, document2) -> Double.compare(ranking.get(document2).getScore(), ranking.get(document1).getScore()))
                    .limit(RESULT_SET_SIZE)
                    .map(document -> new Similarity(document, ranking.get(document).getScore()))
                    .collect(Collectors.toList());
                  
        }

            //DoubleVector tfidfTerms = new DoubleVector();
            //build a matrix with every row = document, column = element in index;
        return null;
    }

}