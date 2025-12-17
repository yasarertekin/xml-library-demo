// DomParserServiceTest.java
package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DOMParserServiceTest {

    private DOMParserService domParserService;
    private String validXml;
    private String invalidXml;

    @BeforeEach
    void setUp() {
        domParserService = new DOMParserService();

        validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-001\">\n" +
                "    <artikel id=\"art-1\">\n" +
                "        <kop>Artikel 1</kop>\n" +
                "        <lid nummer=\"1\">Content 1</lid>\n" +
                "    </artikel>\n" +
                "    <artikel id=\"art-2\">\n" +
                "        <kop>Artikel 2</kop>\n" +
                "        <lid nummer=\"1\">Content 2</lid>\n" +
                "    </artikel>\n" +
                "</wet>";

        invalidXml = "<?xml version=\"1.0\"?><wet><artikel>";
    }

    @Test
    void testParseValidXml() {
        ParseResult result = domParserService.parse(validXml);

        assertNotNull(result);
        assertEquals("DOM", result.getLibrary());
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAantalArtikelen());
        assertTrue(result.getTijdMs() >= 0);
        assertTrue(result.getGeheugenKb() >= 0);
        assertNotNull(result.getArtikelDetails());
        assertFalse(result.getArtikelDetails().isEmpty());
    }

    @Test
    void testParseInvalidXml() {
        ParseResult result = domParserService.parse(invalidXml);

        assertNotNull(result);
        assertEquals("DOM", result.getLibrary());
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertFalse(result.getError().isEmpty());
    }

    @Test
    void testParseEmptyXml() {
        String emptyXml = "<?xml version=\"1.0\"?><wet></wet>";
        ParseResult result = domParserService.parse(emptyXml);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getAantalArtikelen());
    }

    @Test
    void testParseXmlWithMultipleArticles() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 1; i <= 10; i++) {
            xml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        xml.append("</wet>");

        ParseResult result = domParserService.parse(xml.toString());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(10, result.getAantalArtikelen());
        assertTrue(result.getArtikelDetails().size() <= 10);
    }

    @Test
    void testParseNullXml() {
        ParseResult result = domParserService.parse(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testParsePerformanceMetrics() {
        ParseResult result = domParserService.parse(validXml);

        assertTrue(result.getTijdMs() >= 0, "Time should be non-negative");
        assertTrue(result.getGeheugenKb() >= 0, "Memory should be non-negative");
    }
}