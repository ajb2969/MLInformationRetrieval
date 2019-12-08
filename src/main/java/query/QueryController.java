package query;

import retrieval.Models;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import indexer.Index;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import retrieval.Pooling;
import retrieval.Similarity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

@Controller
public class QueryController {
    private static final int HALF_WINDOW_SIZE = 20;
    private final String documentsPath = "documents/";
    private final String htmlDocumentsPath = "html_documents/";
    // final File temp = new File(documentsPath + "temp.txt");
    private ArrayList<Similarity> currDocuments = new ArrayList<>();
    private String currQuery = "";
    private HashSet<Integer> seasons = new HashSet<>();
    private Pooling m;
    private ModelTypes currModel;

    enum Active {
        relevance, alphabet, reverse, seasons,
    };

    public enum ModelTypes {
        BM25, LDA, Pool,
    };

    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("query", new query.Querycontainer());
        return "index";
    }

    @PostMapping("/query")
    public String querySubmit(@ModelAttribute("query") Querycontainer query, Model model) {
        if (query != null) {
            // if (temp.exists()) {
            // temp.delete();
            // }

            if (query.getModel().equals("Pool")) {
                System.out.println("Pooling");
                this.currModel = ModelTypes.Pool;
                m = new Pooling(query.getContent(), ModelTypes.Pool);
            } else if (query.getModel().equals("LDA")) {
                System.out.println("LDA");
                this.currModel = ModelTypes.LDA;
                m = new Pooling(query.getContent(), ModelTypes.LDA);
            } else if (query.getModel().equals("BM25")) {
                this.currModel = ModelTypes.BM25;
                System.out.println("BM25");
                m = new Pooling(query.getContent(), ModelTypes.BM25);
            } else {
                System.out.println("The current model is " + this.currModel.toString());
                m = new Pooling(query.getContent(), this.currModel);
            }
            currQuery = query.getContent();
            ArrayList<Similarity> documents = m.retrieve();

            System.out.println("parsing results");
            if (query.selectedSeason.size() != 0) {
                // documents = (ArrayList<Similarity>) documents.stream().filter(document ->
                // query.selectedSeason.contains(document.getSeason())).collect(Collectors.toList());
                // seasons = new HashSet<>(query.getSelectedSeason());
                // model.addAttribute("seasons", seasons);
            } else {
                // int [] seasonsNumbers = getSeasonNumbers();
                // ArrayList<Integer> seasons = new ArrayList<>();
                // for(int i = 0; i < seasonsNumbers.length;
                // i++){seasons.add(seasonsNumbers[i]);}
                // seasons = new ArrayList<>(seasons);
                // model.addAttribute("seasons", seasons);
            }

            documents = (ArrayList<Similarity>) documents.stream()
                .map(sim -> updateObject(sim, query))
                .collect(Collectors.toList());
            System.out.println("Finished updating documents");
            this.currDocuments = documents;

            // if the first document isn't relevant or similar send back no
            // results available
            model.addAttribute("results", documents);
            model.addAttribute("selected", Active.relevance.toString().toLowerCase());
            model.addAttribute("queryContents", currQuery);
            model.addAttribute("season", "-1");
            model.addAttribute("query", new Querycontainer());
        } else {
            return "index";
        }
        return "result";
    }

    private Similarity updateObject(Similarity sim, Querycontainer query) {
        System.out.println(sim.getDocumentName());
        sim.setPreview(getLongestIncreasingSequence(sim.getDocumentName(), query.getContent()));
        sim.setDocumentLink(Models.HTMLDOCPATHS.get(sim.getDocumentName().substring(0, sim.getDocumentName().length()-4)));
        sim.setDocumentName(Models.HTMLDOCPATHS.get(sim.getDocumentName().substring(0, sim.getDocumentName().length()-4))
                                                    .replaceAll("_", " ")
                                                    .replaceAll(".html", ""));
        return sim;
    }

    @GetMapping("/alphabetically")
    public String alphabetically(@RequestParam(required = true) boolean backward, Model model) {
        ArrayList<Similarity> temp = Lists.newArrayList(this.currDocuments);

        if (backward) {
            sort(temp, (o1, o2) -> o2.getDocumentName().compareTo(o1.getDocumentName()));
        } else {
            sort(temp, Comparator.comparing(Similarity::getDocumentName));
        }
        model.addAttribute("selected",
                !backward ? Active.alphabet.toString().toLowerCase() : Active.reverse.toString().toLowerCase());
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("results", temp);
        model.addAttribute("season", "-1");
        model.addAttribute("queryContents", currQuery);
        model.addAttribute("seasons", seasons);
        return "result";
    }

    @GetMapping("/relevance")
    public String relevance(Model model) {
        model.addAttribute("results", this.currDocuments);
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("selected", Active.relevance.toString().toLowerCase());
        model.addAttribute("season", "-1");
        model.addAttribute("queryContents", currQuery);
        model.addAttribute("seasons", seasons);
        return "result";
    }

    /*
     * @GetMapping("/seasons") public String seasons(@RequestParam(required = true)
     * String season, Model model) { ArrayList<Similarity> temp =
     * Lists.newArrayList(this.currDocuments); temp = (ArrayList<Similarity>)
     * temp.stream() .filter(e -> e.getSeason() == Integer.parseInt(season))
     * .collect(Collectors.toList()); model.addAttribute("results", temp);
     * model.addAttribute("query", new Querycontainer());
     * model.addAttribute("season", season); model.addAttribute("seasons", seasons);
     * model.addAttribute("queryContents", currQuery);
     * model.addAttribute("selected", Active.seasons.toString().toLowerCase());
     * return "result"; }
     */

    @GetMapping("/document")
    @ResponseBody
    public FileSystemResource getDocument(@RequestParam(required = true) String doc, Model model) {
        //TODO replace all links in document with th:href
        if(!doc.contains(".html")) {
            doc += ".html";
        }

        return new FileSystemResource(htmlDocumentsPath + doc);
    }

    private String getLongestIncreasingSequence(String docName, String query) {
        ArrayList<String> terms = (ArrayList<String>) Lists.newArrayList(query.split("\\s+")).stream()
                .filter(term -> !term.isEmpty()).collect(Collectors.toList());
        String fileContent;
        List<Integer> termTokenPositionsInFile;
        try {
            fileContent = new String(Files.readAllBytes(Paths.get(documentsPath + docName)));
            termTokenPositionsInFile = Index.readQueryPositions(terms, docName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileContent = fileContent.replaceAll("<[^>]*>", "");
        List<String> fileTokens = Arrays.stream(fileContent.split("\\s+")).collect(Collectors.toList());

        HashMap<Integer, Integer> windowCenterTermCounts = Maps.newHashMap();
        for (Integer termPosition : termTokenPositionsInFile) {
            int lowerBound = termPosition - HALF_WINDOW_SIZE < 0 ? 0 : termPosition - HALF_WINDOW_SIZE;
            int upperBound = termPosition + HALF_WINDOW_SIZE > fileTokens.size() ? fileTokens.size()
                    : termPosition + HALF_WINDOW_SIZE;
            for (int i = lowerBound; i < upperBound; i++) {
                if (windowCenterTermCounts.containsKey(i)) {
                    Integer currVote = windowCenterTermCounts.get(i);
                    windowCenterTermCounts.put(i, currVote + 1);
                } else {
                    windowCenterTermCounts.put(i, 1);
                }
            }
        }

        Integer bestWindowCenter = windowCenterTermCounts.entrySet().stream()
                .max((center1, center2) -> center1.getValue() > center2.getValue() ? 1 : -1).map(Map.Entry::getKey)
                .orElse(HALF_WINDOW_SIZE);

        int lowerBound = bestWindowCenter - HALF_WINDOW_SIZE < 0 ? 0 : bestWindowCenter - HALF_WINDOW_SIZE;
        int upperBound = bestWindowCenter + HALF_WINDOW_SIZE > fileTokens.size() ? fileTokens.size()
                : bestWindowCenter + HALF_WINDOW_SIZE;
        return String.join(" ", fileTokens.subList(lowerBound, upperBound));
    }
}