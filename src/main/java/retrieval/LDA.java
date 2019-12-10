package retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LDA extends Models {
    final String vecModelFilePath = "indicies/vecSpaceModel.tsv";
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
        if(!outputFile.exists()) {
            List<String> documents = new ArrayList<>(Models.getAllDocuments());
            Collections.sort(documents);
            System.out.println("There are " + documents.size() + " documents");
            docTokens = new ArrayList<>(Models.getTokenToEntryIndex().keySet());
            Collections.sort(docTokens);
            System.out.println("There are " + docTokens.size() + " tokens");
        
            try {
                Scheduler schedule = new Scheduler(documents, docTokens, outputFile);
                parsedDocs = schedule.executeParse();
                System.out.println("The hashmap has a size of " + parsedDocs.keySet().size());
                System.out.println("Finished lda");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            
            parsedDocs = new ConcurrentHashMap<>();
            System.out.println("Reading in compressed sparse matrix");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                String line;
                while((line = reader.readLine()) != null) {
                    String [] entries = line.split("\t");
                    String key = entries[0];
                    ArrayList<LDA.Index> values = new ArrayList<>();
                    for(int index = 1; index < entries.length; index++) {
                        String [] column = entries[index].split(",");
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
            //http://www.jmlr.org/papers/volume3/blei03a/blei03a.pdf
            double mu = 1000;
            HashMap<String, TFSmoothing> ranking = new HashMap<>();
            int totalCollectionTokens = Models.getDocumentTokenSizes().keySet().stream().mapToInt(key -> Models.getDocumentTokenSizes().get(key)).sum();
            for(String term: terms) {
                int collectionTermsQuantity = Models.getTermToFileAndOccurrence().get(term).values().stream().reduce(0, Integer::sum);
                for(String document: parsedDocs.keySet()) {
                    if(!ranking.containsKey(document)) {
                        TFSmoothing termTF = Models.termFreqencySmoothing(term, document);
                        termTF.setMu(mu);
                        termTF.setSmoothing(collectionTermsQuantity, totalCollectionTokens);
                        ranking.put(document, termTF);
                    } else {
                        
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