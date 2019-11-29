package retrieval;

import java.util.Objects;

public class Similarity implements Comparable<Similarity> {
    private String document_name;
    private String preview;
    private int season;
    private double similarity;

    Similarity(String document, double similarity, int season) {
        this.document_name = document;
        this.similarity = similarity;
        this.preview = "";
        this.season = season;
    }

    public String getDocument_name() {
        return document_name.replace(".txt", "");
    }

    public String getDocumentLink() {
        return document_name;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(Similarity o) {
        return Double.compare(this.getSimilarity(), o.getSimilarity());
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getDocument_name());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Similarity) {
            if(this.getDocument_name().equals(((Similarity) obj).getDocument_name())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
