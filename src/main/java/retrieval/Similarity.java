package retrieval;

import java.util.Objects;

public class Similarity implements Comparable<Similarity> {
    private String document_name;
    private String documentLink;
    private String preview;
    private double similarity;

    Similarity(String document, double similarity) {
        this.document_name = document;
        this.similarity = similarity;
        this.preview = "";
        this.documentLink = "";
    }

    public void setDocumentName(String name) {
        this.document_name = name;
    }
    public String getDocumentName() {
        return this.document_name;
    }

    public void setDocumentLink(String link) {
        this.documentLink = link;
    }

    public String getDocumentLink() {
        return this.documentLink;
    }

    public double getSimilarity() {
        return this.similarity;
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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getDocumentName());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Similarity) {
            if(this.getDocumentName().equals(((Similarity) obj).getDocumentName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
