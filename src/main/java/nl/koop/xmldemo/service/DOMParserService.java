package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;

@Service
public class DOMParserService {

    public ParseResult parse(String xmlContent) {
        ParseResult result = new ParseResult("DOM");

        try {
            System.gc();
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            NodeList artikelen = doc.getElementsByTagName("artikel");
            result.setAantalArtikelen(artikelen.getLength());

            int limit = Math.min(10, artikelen.getLength());
            for (int i = 0; i < limit; i++) {
                Element artikel = (Element) artikelen.item(i);
                String id = artikel.getAttribute("id");
                NodeList kopNodes = artikel.getElementsByTagName("kop");
                String kop = kopNodes.getLength() > 0 ? kopNodes.item(0).getTextContent() : "Geen kop";
                result.addArtikelDetail(id + ": " + kop);
            }

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);

        } catch (Exception e) {
            result.setError("DOM parsing error: " + e.getMessage());
        }

        return result;
    }
}