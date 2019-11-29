package parser;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.List;

public class DocumentGenerationRunner {
    public static void main(String[] args) {
        HTMLParser parser = new HTMLParser();
        for (File f : new File(HTMLParser.HTML_LOCATION).listFiles()) {
            Document transcriptsDocument = parser.getDocument(f);
            List<EpisodeDocument> episodeTranscripts = parser.getEpisodeTranscripts(transcriptsDocument);
            episodeTranscripts.forEach(DocumentWriter::writeDocumentToFile);
        }
    }
}
