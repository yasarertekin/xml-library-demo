package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import nl.koop.xmldemo.model.Wet;
import org.springframework.stereotype.Service;
import jakarta.xml.bind.*;
import java.io.StringReader;

@Service
public class JAXBParserService {

    public ParseResult parse(String xmlContent) {
        ParseResult result = new ParseResult("JAXB");

        try {
            System.gc();
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            JAXBContext context = JAXBContext.newInstance(Wet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Wet wet = (Wet) unmarshaller.unmarshal(new StringReader(xmlContent));

            int aantalArtikelen = 0;
            if (wet.getHoofdstukken() != null) {
                for (var hoofdstuk : wet.getHoofdstukken()) {
                    if (hoofdstuk.getArtikelen() != null) {
                        for (var artikel : hoofdstuk.getArtikelen()) {
                            aantalArtikelen++;
                            if (result.getArtikelDetails().size() < 10) {
                                result.addArtikelDetail(artikel.getId() + ": " + artikel.getKop());
                            }
                        }
                    }
                }
            }

            result.setAantalArtikelen(aantalArtikelen);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);

        } catch (Exception e) {
            result.setError("JAXB parsing error: " + e.getMessage());
        }

        return result;
    }
}