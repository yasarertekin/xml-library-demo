package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ComparisonResult;
import nl.koop.xmldemo.dto.ParseResult;
import org.springframework.stereotype.Service;

@Service
public class XmlComparisonService {

    private final DOMParserService domParser;
    private final SAXParserService saxParser;
    private final StAXParserService staxParser;
    private final JAXBParserService jaxbParser;

    public XmlComparisonService(DOMParserService domParser,
                                SAXParserService saxParser,
                                StAXParserService staxParser,
                                JAXBParserService jaxbParser) {
        this.domParser = domParser;
        this.saxParser = saxParser;
        this.staxParser = staxParser;
        this.jaxbParser = jaxbParser;
    }

    public ComparisonResult compareAllLibraries(String xmlContent) {
        ComparisonResult comparison = new ComparisonResult();

        System.out.println("\n=== Starting XML Parsing Comparison ===");

        System.out.println("Testing DOM...");
        ParseResult domResult = domParser.parse(xmlContent);
        comparison.addResult(domResult);
        printResult(domResult);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        System.out.println("Testing SAX...");
        ParseResult saxResult = saxParser.parse(xmlContent);
        comparison.addResult(saxResult);
        printResult(saxResult);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        System.out.println("Testing StAX...");
        ParseResult staxResult = staxParser.parse(xmlContent);
        comparison.addResult(staxResult);
        printResult(staxResult);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        System.out.println("Testing JAXB...");
        ParseResult jaxbResult = jaxbParser.parse(xmlContent);
        comparison.addResult(jaxbResult);
        printResult(jaxbResult);

        comparison.berekenStatistieken();

        System.out.println("\n=== Comparison Complete ===");
        System.out.println("Snelste: " + comparison.getSnelste());
        System.out.println("Minste geheugen: " + comparison.getMinsteGeheugen());
        System.out.println("Aanbeveling: " + comparison.getAanbeveling());
        System.out.println("=============================\n");

        return comparison;
    }

    private void printResult(ParseResult result) {
        if (result.isSuccess()) {
            System.out.println("  ✓ " + result.getLibrary() + ": " +
                    result.getAantalArtikelen() + " artikelen, " +
                    result.getTijdMs() + "ms, " +
                    result.getGeheugenKb() + "KB");
        } else {
            System.out.println("  ✗ " + result.getLibrary() + ": " + result.getError());
        }
    }

    public String generateLargeXml(int aantalArtikelen) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<wet id=\"wet-groot\">\n");
        xml.append("  <metadata>\n");
        xml.append("    <titel>Grote Test Wet</titel>\n");
        xml.append("    <datum>2024-12-15</datum>\n");
        xml.append("  </metadata>\n");

        for (int i = 1; i <= aantalArtikelen; i++) {
            xml.append("  <hoofdstuk nummer=\"").append((i-1)/10 + 1).append("\">\n");
            xml.append("    <titel>Hoofdstuk ").append((i-1)/10 + 1).append("</titel>\n");
            xml.append("    <artikel id=\"art-").append(i).append("\">\n");
            xml.append("      <kop>Artikel ").append(i).append("</kop>\n");
            for (int j = 1; j <= 3; j++) {
                xml.append("      <lid nummer=\"").append(j).append("\">")
                        .append("Lid ").append(j).append(" van artikel ").append(i)
                        .append("</lid>\n");
            }
            xml.append("    </artikel>\n");
            xml.append("  </hoofdstuk>\n");
        }

        xml.append("</wet>");
        return xml.toString();
    }
}