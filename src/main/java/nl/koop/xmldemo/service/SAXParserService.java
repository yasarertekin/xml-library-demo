package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.springframework.stereotype.Service;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SAXParserService {

    public ParseResult parse(String xmlContent) {
        ParseResult result = new ParseResult("SAX");

        try {
            System.gc();
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            ArtikelHandler handler = new ArtikelHandler();
            parser.parse(new ByteArrayInputStream(xmlContent.getBytes()), handler);

            result.setAantalArtikelen(handler.getAantalArtikelen());
            result.setArtikelDetails(handler.getDetails());

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);

        } catch (Exception e) {
            result.setError("SAX parsing error: " + e.getMessage());
        }

        return result;
    }

    private static class ArtikelHandler extends DefaultHandler {
        private int aantalArtikelen = 0;
        private List<String> details = new ArrayList<>();
        private boolean inKop = false;
        private StringBuilder kopText = new StringBuilder();
        private String currentArtikelId = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if ("artikel".equals(qName)) {
                aantalArtikelen++;
                currentArtikelId = attrs.getValue("id");
            } else if ("kop".equals(qName)) {
                inKop = true;
                kopText.setLength(0);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (inKop) {
                kopText.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("kop".equals(qName)) {
                if (details.size() < 10) {
                    details.add(currentArtikelId + ": " + kopText.toString().trim());
                }
                inKop = false;
            }
        }

        public int getAantalArtikelen() {
            return aantalArtikelen;
        }

        public List<String> getDetails() {
            return details;
        }
    }
}