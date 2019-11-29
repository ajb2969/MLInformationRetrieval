package parser;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class HTMLParser {
    private static final String HTML_LOCATION = System.getProperty("user.dir") + "/data/transcripts.html";
    private static final String HTML_TRANSCRIPT_ID = "mw-content-text";
    private static final String HTML_TOC_ID = "transcripts-toc";
    private static final Set<String> IGNORED_TAGS = ImmutableSet.of("p", "div", "noscript", "h3", "h4");
    private Map<String, Integer> titleToSeason = Maps.newHashMap();

    Document getDocument() {
        File transcriptsFile = new File(HTML_LOCATION);

        try {
            return Jsoup.parse(transcriptsFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<EpisodeDocument> getEpisodeTranscripts(Document transciptsDoc) {
        Element tableOfContents = transciptsDoc.getElementById(HTML_TOC_ID);
        Elements seasonLists = tableOfContents.getElementsByAttributeValue("style", "vertical-align:top;");
        seasonLists.forEach(this::processSeasonList);

        Element mainContent = transciptsDoc.getElementById(HTML_TRANSCRIPT_ID);
        Elements contentList = mainContent.children();

        List<Element> transcriptElements = contentList.stream()
            .filter(htmlElement -> !IGNORED_TAGS.contains(htmlElement.tagName()))
            .collect(Collectors.toList());

        HashMap<String, List<Element>> titleTranscriptElementsMap = convertToTitleScriptMap(transcriptElements);

        return titleTranscriptElementsMap.entrySet().stream()
            .map(entry -> convert(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private void processSeasonList(Element seasonList) {
        int seasonNum = Integer.parseInt(seasonList.getElementsByTag("h3").first().text().split(" ")[1]);
        seasonList.getElementsByTag("a").stream()
            .map(Element::text)
            .forEach(ep -> titleToSeason.put(ep, seasonNum));
    }

    private HashMap<String, List<Element>> convertToTitleScriptMap(List<Element> transcriptElements) {
        HashMap<String, List<Element>> titleToScriptElements = Maps.newHashMap();

        String currentTitle = "";
        ArrayList<Element> currentElements = Lists.newArrayList();
        for (Element htmlElement : transcriptElements) {
            String tagName = htmlElement.tagName();
            TranscriptTag tag = TranscriptTag.from(tagName);
            switch (tag) {
                case TITLE_TAG:
                    if (!currentTitle.isEmpty()) {
                        titleToScriptElements.put(currentTitle, currentElements);
                    }
                    TextNode titleNode = (TextNode) htmlElement.child(0).childNode(0);
                    currentTitle = titleNode.text();
                    currentElements = Lists.newArrayList();
                    break;
                case PARAGRAPH_TAG:
                    currentElements.add(htmlElement);
                    break;
                case TABLE_TAG:
                    Elements lines = htmlElement.getElementsByTag(TranscriptTag.PARAGRAPH_TAG.getTagName());
                    currentElements.addAll(lines);
                    break;
                default:
                    System.err.println("Unknown tag " + tag.toString());
                    throw new RuntimeException();
            }

            if (currentTitle.equals("My Little Pony Equestria Girls")) {
                // end of friendship is magic
                break;
            }
        }
        return titleToScriptElements;
    }

    private EpisodeDocument convert(String title, List<Element> htmlTranscript) {
        List<String> bodySections = htmlTranscript.stream()
            .map(this::extractText)
            .collect(Collectors.toList());
        int season = titleToSeason.get(title);
        return new EpisodeDocument(title, String.join("", bodySections), season);
    }

    private String extractText(Element section) {
        String tagName = section.tagName();
        if (tagName.equals(TranscriptTag.BOLD_TAG.getTagName()) ||
            tagName.equals(TranscriptTag.ITALICIZE_TAG.getTagName()) ||
            tagName.equals(TranscriptTag.SMALL_TAG.getTagName()) ||
            tagName.equals(TranscriptTag.BIG_TAG.getTagName()) ||
            tagName.equals(TranscriptTag.SUB_TAG.getTagName())) {
            return section.toString();
        } else if (tagName.equals(TranscriptTag.PARAGRAPH_TAG.getTagName()) ||
                   tagName.equals(TranscriptTag.INNER_PARAGRAPH_TAG.getTagName()) ||
                   tagName.equals(TranscriptTag.SPAN_TAG.getTagName()) ||
                   tagName.equals(TranscriptTag.LINK_TAG.getTagName())) {
            List<String> lines = section.childNodes().stream()
                .map(this::extractTextHelper)
                .collect(Collectors.toList());

            return String.join("", lines);
        } else {
            System.err.println("Unknown tag");
            throw new RuntimeException();
        }
    }

    private String extractTextHelper(Node childNode) {
        if (childNode instanceof TextNode) {
            return ((TextNode) childNode).getWholeText();
        } else if (childNode instanceof Element) {
            return extractText((Element) childNode);
        } else {
            System.err.println("Unknown tag");
            throw new RuntimeException();
        }
    }
}
