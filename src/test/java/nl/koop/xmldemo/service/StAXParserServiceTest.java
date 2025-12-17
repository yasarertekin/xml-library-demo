// StaxParserServiceTest.java
package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StAXParserServiceTest {

    private StAXParserService staxParserService;
    private String validXml;
    private String invalidXml;

    @BeforeEach
    void setUp() {
        staxParserService = new StAXParserService();

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
        ParseResult result = staxParserService.parse(validXml);

        assertNotNull(result);
        assertEquals("StAX", result.getLibrary());
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAantalArtikelen());
        assertTrue(result.getTijdMs() >= 0);
        assertTrue(result.getGeheugenKb() >= 0);
        assertNotNull(result.getArtikelDetails());
    }

    @Test
    void testParseInvalidXml() {
        ParseResult result = staxParserService.parse(invalidXml);

        assertNotNull(result);
        assertEquals("StAX", result.getLibrary());
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testParseEmptyXml() {
        String emptyXml = "<?xml version=\"1.0\"?><wet></wet>";
        ParseResult result = staxParserService.parse(emptyXml);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getAantalArtikelen());
    }

    @Test
    void testParseXmlWithAttributes() {
        String xmlWithAttrs = "<?xml version=\"1.0\"?><wet>" +
                "<artikel id=\"art-1\" type=\"main\">" +
                "<kop>Test</kop>" +
                "</artikel></wet>";

        ParseResult result = staxParserService.parse(xmlWithAttrs);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getAantalArtikelen());
    }

    @Test
    void testParseNullXml() {
        ParseResult result = staxParserService.parse(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testStaxPullParsing() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 1; i <= 50; i++) {
            xml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        xml.append("</wet>");

        ParseResult result = staxParserService.parse(xml.toString());

        assertTrue(result.isSuccess());
        assertEquals(50, result.getAantalArtikelen());
    }
}
