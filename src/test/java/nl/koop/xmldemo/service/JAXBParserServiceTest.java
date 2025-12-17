// JaxbParserServiceTest.java
package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JAXBParserServiceTest {

    private JAXBParserService jaxbParserService;
    private String validXml;
    private String invalidXml;

    @BeforeEach
    void setUp() {
        jaxbParserService = new JAXBParserService();

        // CORRECTE XML structuur met <hoofdstuk> element
        validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-001\">\n" +
                "    <metadata>\n" +
                "        <titel>Test Wet</titel>\n" +
                "        <datum>2024-01-01</datum>\n" +
                "    </metadata>\n" +
                "    <hoofdstuk nummer=\"1\">\n" +
                "        <titel>Hoofdstuk 1</titel>\n" +
                "        <artikel id=\"art-1\">\n" +
                "            <kop>Artikel 1</kop>\n" +
                "            <lid nummer=\"1\">Content 1</lid>\n" +
                "        </artikel>\n" +
                "        <artikel id=\"art-2\">\n" +
                "            <kop>Artikel 2</kop>\n" +
                "            <lid nummer=\"1\">Content 2</lid>\n" +
                "        </artikel>\n" +
                "    </hoofdstuk>\n" +
                "</wet>";

        invalidXml = "<?xml version=\"1.0\"?><wet><artikel>";
    }

    @Test
    void testParseValidXml() {
        ParseResult result = jaxbParserService.parse(validXml);

        assertNotNull(result, "Result should not be null");
        assertEquals("JAXB", result.getLibrary());
        assertTrue(result.isSuccess(), "Parsing should succeed: " + result.getError());
        assertEquals(2, result.getAantalArtikelen(), "Should find 2 articles");
        assertTrue(result.getTijdMs() >= 0, "Time should be non-negative");
        assertTrue(result.getGeheugenKb() >= 0, "Memory should be non-negative");
        assertNotNull(result.getArtikelDetails());
    }

    @Test
    void testParseInvalidXml() {
        ParseResult result = jaxbParserService.parse(invalidXml);

        assertNotNull(result);
        assertEquals("JAXB", result.getLibrary());
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("JAXB parsing error") ||
                result.getError().contains("Premature end"));
    }

    @Test
    void testParseEmptyXml() {
        String emptyXml = "<?xml version=\"1.0\"?>\n" +
                "<wet id=\"wet-empty\">\n" +
                "    <metadata><titel>Empty</titel><datum>2024-01-01</datum></metadata>\n" +
                "</wet>";
        ParseResult result = jaxbParserService.parse(emptyXml);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Empty wet should parse successfully");
        assertEquals(0, result.getAantalArtikelen(), "Should have 0 articles");
    }

    @Test
    void testParseWithJaxbAnnotations() {
        ParseResult result = jaxbParserService.parse(validXml);

        assertTrue(result.isSuccess(), "Should parse successfully: " + result.getError());
        assertEquals(2, result.getAantalArtikelen());
        assertNotNull(result.getArtikelDetails());
        assertEquals(2, result.getArtikelDetails().size(), "Should have 2 article details");
        assertTrue(result.getArtikelDetails().get(0).contains("art-1"));
        assertTrue(result.getArtikelDetails().get(1).contains("art-2"));
    }

    @Test
    void testParseNullXml() {
        ParseResult result = jaxbParserService.parse(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("JAXB parsing error"));
    }

    @Test
    void testJaxbTypeMapping() {
        ParseResult result = jaxbParserService.parse(validXml);

        assertNotNull(result);
        assertEquals("JAXB", result.getLibrary());
        assertTrue(result.isSuccess(), "JAXB should map XML to Java objects");
        assertTrue(result.getAantalArtikelen() > 0);
    }

    @Test
    void testJaxbPerformance() {
        // Generate XML with CORRECT structure (met hoofdstuk!)
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<wet id=\"wet-perf\">\n");
        xml.append("    <metadata><titel>Performance Test</titel><datum>2024-01-01</datum></metadata>\n");
        xml.append("    <hoofdstuk nummer=\"1\">\n");
        xml.append("        <titel>Performance Hoofdstuk</titel>\n");

        for (int i = 1; i <= 20; i++) {
            xml.append("        <artikel id=\"art-").append(i).append("\">\n");
            xml.append("            <kop>Artikel ").append(i).append("</kop>\n");
            xml.append("            <lid nummer=\"1\">Content ").append(i).append("</lid>\n");
            xml.append("        </artikel>\n");
        }

        xml.append("    </hoofdstuk>\n");
        xml.append("</wet>");

        ParseResult result = jaxbParserService.parse(xml.toString());

        assertTrue(result.isSuccess(), "Should parse successfully: " + result.getError());
        assertEquals(20, result.getAantalArtikelen(), "Should find 20 articles");
        assertTrue(result.getTijdMs() >= 0, "Time should be measured");
        assertTrue(result.getTijdMs() < 5000, "Should complete within 5 seconds");
    }

    @Test
    void testMultipleHoofdstukken() {
        String multiHoofdstukXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-multi\">\n" +
                "    <metadata><titel>Multi Test</titel><datum>2024-01-01</datum></metadata>\n" +
                "    <hoofdstuk nummer=\"1\">\n" +
                "        <titel>Hoofdstuk 1</titel>\n" +
                "        <artikel id=\"art-1\"><kop>Artikel 1</kop></artikel>\n" +
                "        <artikel id=\"art-2\"><kop>Artikel 2</kop></artikel>\n" +
                "    </hoofdstuk>\n" +
                "    <hoofdstuk nummer=\"2\">\n" +
                "        <titel>Hoofdstuk 2</titel>\n" +
                "        <artikel id=\"art-3\"><kop>Artikel 3</kop></artikel>\n" +
                "    </hoofdstuk>\n" +
                "</wet>";

        ParseResult result = jaxbParserService.parse(multiHoofdstukXml);

        assertTrue(result.isSuccess(), "Should parse multiple hoofdstukken");
        assertEquals(3, result.getAantalArtikelen(), "Should find 3 articles across 2 hoofdstukken");
    }
}