package testing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CombineQrelFilesRunner {
    private static final String TRAINING_QRELS_FILENAME = "/training.qrels";

    public static void main(String[] args) {
        try {
            combineQrelFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void combineQrelFiles() throws IOException {
        List<String> qrels = new ArrayList<>();
        File[] groupDirectories = new File(GridSearch.TRAINING_QUERIES_BASE_PATH).listFiles();
        for (File group : groupDirectories) {
            if (!group.getName().equals("results") && !group.getName().equals("training.qrels")) {
                List<String> groupQrels = getGroupContents(group);
                qrels.addAll(groupQrels);
            }
        }
        writeCombinedQrelFile(String.join("\n", qrels));
    }

    private static List<String> getGroupContents(File groupDirectory) throws IOException {
        List<String> qrels = new ArrayList<>();
        String groupNumber = groupDirectory.getName();

        System.out.println("Getting Qrels for Group " + groupNumber);

        File[] groupContents = groupDirectory.listFiles();
        for (File file : groupContents) {
            if (!file.getName().equals("queries.txt")) {
                List<String> fileContents = getQrelFileContents(file, groupNumber);
                qrels.addAll(fileContents);
            }
        }
        return qrels;
    }

    private static List<String> getQrelFileContents(File qrelFile, String groupNumber) throws IOException {
        System.out.println("\tProcessing file " + qrelFile.getName());
        BufferedReader br = new BufferedReader(new FileReader(qrelFile));
        ArrayList<String> qrels = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            String groupedQrel = groupNumber + trimmedLine;
            qrels.add(groupedQrel);
        }
        br.close();
        return qrels;
    }

    private static void writeCombinedQrelFile(String qrels) throws IOException {
        File resultsFile = new File(GridSearch.TRAINING_QUERIES_BASE_PATH + TRAINING_QRELS_FILENAME);
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile));
        bw.write(qrels);
        bw.flush();
        bw.close();
    }
}
