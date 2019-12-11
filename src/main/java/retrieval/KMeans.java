package retrieval;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import retrieval.LDA.Index;

public class KMeans {

    public static ConcurrentHashMap<String, List<Index>> readSparseMatrix(String outputFile) {
        ConcurrentHashMap<String, List<Index>> parsedDocs = new ConcurrentHashMap<>();
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

        return parsedDocs;
    }

    public static void main() {
        ConcurrentHashMap<String, List<Index>> parsedDocs = readSparseMatrix(LDA.vecModelFilePath);
        SparseMatrix sm = new SparseMatrix(parsedDocs, Models.getTokenToEntryIndex().keySet().size());
        
    }


}