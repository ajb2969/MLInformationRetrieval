package retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LDA extends Models {

    public LDA() {
        super();
    }

    static class Index {
        private int index;
        private double value;

        Index(int index, double value) {
            this.index = index;
            this.value = value;
        }
    }

    public ArrayList<Similarity> retrieve(String query) {
        System.out.println("Retrieving LDA");
        String[] terms = extractTerms(query);
        Set<String> documents = Models.getAllDocuments();

        System.out.println("There are " + documents.size() + " documents");
        List<String> docTokens = new ArrayList<>(Models.getTokenToEntryIndex().keySet());
        Collections.sort(docTokens);
        System.out.println("There are " + docTokens.size() + " tokens");

        Scheduler schedule = new Scheduler(documents, docTokens);
        ConcurrentHashMap<String, ArrayList<Index>> parsedDocs;

        try {
            parsedDocs = schedule.executeParse();
            System.out.println("The hashmap has a size of " + parsedDocs.keySet().size());
            System.out.println("Finished lda");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
            //DoubleVector tfidfTerms = new DoubleVector();

            //build a matrix with every row = document, column = element in index;
        return null;
    }

}