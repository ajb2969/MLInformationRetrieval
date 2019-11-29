package parser;

public class EpisodeDocument {
    private String title;
    private int season;
    private String body;

    EpisodeDocument(String title, String body, int season) {
        this.title = title;
        this.body = body;
        this.season = season;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }
}
