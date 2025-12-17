package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.springframework.stereotype.Service;
import javax.xml.stream.*;
import java.io.ByteArrayInputStream;

@Service
public class StAXParserService {

    public ParseResult parse(String xmlContent) {
        ParseResult result = new ParseResult("StAX");

        try {
            System.gc();
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(xmlContent.getBytes())
            );

            int aantalArtikelen = 0;
            String currentArtikelId = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();

                    if ("artikel".equals(elementName)) {
                        aantalArtikelen++;
                        currentArtikelId = reader.getAttributeValue(null, "id");
                    } else if ("kop".equals(elementName) && result.getArtikelDetails().size() < 10) {
                        String kopText = reader.getElementText();
                        result.addArtikelDetail(currentArtikelId + ": " + kopText);
                    }
                }
            }

            reader.close();

            result.setAantalArtikelen(aantalArtikelen);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);

        } catch (Exception e) {
            result.setError("StAX parsing error: " + e.getMessage());
        }

        return result;
    }
}