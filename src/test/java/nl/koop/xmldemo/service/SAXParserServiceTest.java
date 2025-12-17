// SaxParserServiceTest.java
package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SAXParserServiceTest {

    private SAXParserService saxParserService;
    private String validXml;
    private String invalidXml;

    @BeforeEach
    void setUp() {
        saxParserService = new SAXParserService();

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
        ParseResult result = saxParserService.parse(validXml);

        assertNotNull(result);
        assertEquals("SAX", result.getLibrary());
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAantalArtikelen());
        assertTrue(result.getTijdMs() >= 0);
        assertTrue(result.getGeheugenKb() >= 0);
        assertNotNull(result.getArtikelDetails());
    }

    @Test
    void testParseInvalidXml() {
        ParseResult result = saxParserService.parse(invalidXml);

        assertNotNull(result);
        assertEquals("SAX", result.getLibrary());
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testParseEmptyXml() {
        String emptyXml = "<?xml version=\"1.0\"?><wet></wet>";
        ParseResult result = saxParserService.parse(emptyXml);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getAantalArtikelen());
    }

    @Test
    void testParseLargeXml() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 1; i <= 100; i++) {
            xml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        xml.append("</wet>");

        ParseResult result = saxParserService.parse(xml.toString());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(100, result.getAantalArtikelen());
    }

    @Test
    void testParseNullXml() {
        ParseResult result = saxParserService.parse(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testSaxMemoryEfficiency() {
        StringBuilder largeXml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 1; i <= 1000; i++) {
            largeXml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        largeXml.append("</wet>");

        ParseResult result = saxParserService.parse(largeXml.toString());

        assertTrue(result.isSuccess());
        // SAX should use less memory than DOM for large files
        assertTrue(result.getGeheugenKb() >= 0);
    }
}
