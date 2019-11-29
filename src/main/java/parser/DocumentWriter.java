package parser;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class DocumentWriter {
    private static final String DOC_DIR = System.getProperty("user.dir") + "/documents";

    static void writeDocumentToFile(EpisodeDocument document) {
        String season = "Season " + String.valueOf(document.getSeason());

        new File(DOC_DIR).mkdir();
        List<String> lines = Lists.asList(document.getTitle(), season, document.getBody().split("\n"));
        Path path = Paths.get(DOC_DIR + "/" + document.getTitle() + ".txt");
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error when writing to file: " + document.getTitle());
            throw new RuntimeException();
        }
    }
}
