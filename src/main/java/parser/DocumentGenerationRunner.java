package parser;

import org.jsoup.nodes.Document;

import java.util.List;

public class DocumentGenerationRunner {
    public static void main(String[] args) {
        HTMLParser parser = new HTMLParser();
        Document transcriptsDocument = parser.getDocument();
        List<EpisodeDocument> episodeTranscripts = parser.getEpisodeTranscripts(transcriptsDocument);
        episodeTranscripts.forEach(DocumentWriter::writeDocumentToFile);
    }
}
