package retrieval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.stream.Collectors;

public class BM25 extends Models {
    private static final double K1 = .8;
    private static final double K2 = 500; // typical values range from 0 to 1000
    private static final double B = 0.75; // normalizes the tf componenet of the doc frequencies

    public BM25() {
        super();
    }

    @Override
    public ArrayList<Similarity> retrieve(String query) {
        ArrayList<String> keywords = Lists.newArrayList(super.extractTerms(query));
        Set<String> relevantDocuments = getRelevantDocuments(keywords);
        ArrayList<String> documentCollection = getDocumentList();

        Map<String, Double> scoredDocuments = Maps.newHashMap();
        documentCollection.stream()
            .forEach(document -> scoredDocuments.put(document, getScore(keywords, document, relevantDocuments)));

        return (ArrayList<Similarity>) scoredDocuments.entrySet().stream()
            .map(entry -> new Similarity(entry.getKey(), entry.getValue(), DOCS_TO_SEASONS.get(entry.getKey())))
            .sorted(Comparator.reverseOrder())
            .limit(Models.DOCUMENTSRETURNED)
            .collect(Collectors.toList());
    }

    private Set<String> getRelevantDocuments(List<String> keywords) {
        double threshold = keywords.stream()
            .mapToDouble(this::getAverageTfIdfForCollection)
            .average()
            .orElse(1); // highest threshold if no keywords
        return getDocumentList().stream()
            .filter(document -> getAverageTfIdfForTerms(keywords, document) > threshold)
            .collect(Collectors.toSet());
    }

    private double getAverageTfIdfForTerms(List<String> keywords, String document) {
        return keywords.stream()
            .mapToDouble(term -> TfIdf.tfidf(term, document))
            .average()
            .orElse(0);
    }

    private double getAverageTfIdfForCollection(String term) {
        if (!get_doc_indicies().containsKey(term)) {
            return 0; // if term isnt in collection, term frequency = 0
        }
        double tfidfSum = get_doc_indicies().get(term).getFileOccurrences().stream()
            .map(FileOccurrence::getFilename)
            .mapToDouble(document -> TfIdf.tfidf(term, document))
            .sum();

        double numberOfDocuments = getNumberOfDocuments();
        return tfidfSum / numberOfDocuments;
    }

    private double getScore(List<String> keywords, String filename, Set<String> relevantDocuments) {
        return keywords.stream()
            .mapToDouble(word -> getScore(word, filename, relevantDocuments, keywords))
            .sum();
    }

    private double getScore(String term, String filename, Set<String> relevantDocuments, List<String> keywords) {
        return relevance(term, relevantDocuments) * documentFrequency(term, filename) * queryFrequency(term, keywords);
    }

    private double relevance(String term, Set<String> relevantDocuments) {
        double N = getNumberOfDocuments();
        double ni = getNumberOfDocuments(term);
        double R = relevantDocuments.size();
        double ri = getNumberOfDocumentsContainingTerm(term, relevantDocuments);

        double numerator = (ri + 0.5) / (R - ri + 0.5);
        double denomerator = (ni - ri + 0.5) / (N - ni - R + ri + 0.5);

        return Math.log(numerator / denomerator);
    }

    private double documentFrequency(String term, String filename) {
        double fileFreq = getOccurrencesInFile(term, filename);

        return ((K1 + 1) * fileFreq) / (k(filename) + fileFreq);
    }

    private double queryFrequency(String term, List<String> keywords) {
        long queryFreq = keywords.stream()
            .filter(word -> word.equals(term))
            .count();

        return ((K2 + 1) * queryFreq) / (K2 + queryFreq);
    }

    private double k(String filename) {
        return K1 * ((1 - B) + (B * getDocLength(filename) / averageDocLength()));
    }

    private double getDocLength(String filename) {
        return (double) getFileTermSize().get(filename);
    }

    private double averageDocLength() {
        return getFileTermSize().values().stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0);
    }

    private double getNumberOfDocuments() {
        return getFileTermSize().size();
    }

    private double getNumberOfDocuments(String term) {
        return occursInDocuments(term) ? get_doc_indicies().get(term).getSize() : 0;
    }

    private double getNumberOfDocumentsContainingTerm(String term, Set<String> documents) {
        return !occursInDocuments(term) ? 0 : get_doc_indicies().get(term).getFileOccurrences().stream()
            .map(FileOccurrence::getFilename)
            .filter(documents::contains)
            .count();
    }
}
