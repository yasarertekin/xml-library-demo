package nl.koop.xmldemo.service;

import nl.koop.xmldemo.dto.ComparisonResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XmlComparisonServiceTest {

    private XmlComparisonService xmlComparisonService;
    private String validXml;

    @BeforeEach
    void setUp() {
        DOMParserService domParser = new DOMParserService();
        SAXParserService saxParser = new SAXParserService();
        StAXParserService staxParser = new StAXParserService();
        JAXBParserService jaxbParser = new JAXBParserService();

        xmlComparisonService = new XmlComparisonService(
                domParser, saxParser, staxParser, jaxbParser
        );

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
    }

    @Test
    void testCompareAllLibraries() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries(validXml);

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(4, result.getResults().size(), "Should have results for all 4 libraries");

        // Verify all libraries were tested
        assertTrue(result.getResults().stream().anyMatch(r -> "DOM".equals(r.getLibrary())));
        assertTrue(result.getResults().stream().anyMatch(r -> "SAX".equals(r.getLibrary())));
        assertTrue(result.getResults().stream().anyMatch(r -> "StAX".equals(r.getLibrary())));
        assertTrue(result.getResults().stream().anyMatch(r -> "JAXB".equals(r.getLibrary())));

        // Verify statistics were calculated
        assertNotNull(result.getSnelste());
        assertNotNull(result.getMinsteGeheugen());
        assertNotNull(result.getAanbeveling());
    }

    @Test
    void testCompareAllLibrariesWithInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\"?><invalid>";

        ComparisonResult result = xmlComparisonService.compareAllLibraries(invalidXml);

        assertNotNull(result);
        assertEquals(4, result.getResults().size());

        // At least some parsers should fail with invalid XML
        long failedParsers = result.getResults().stream()
                .filter(r -> !r.isSuccess())
                .count();
        assertTrue(failedParsers > 0, "At least one parser should fail with invalid XML");
    }

    @Test
    void testAllLibrariesFindSameNumberOfArticles() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries(validXml);

        // All successful parsers should find the same number of articles
        long successfulCount = result.getResults().stream()
                .filter(r -> r.isSuccess())
                .count();

        assertTrue(successfulCount > 0, "At least one parser should succeed");

        if (successfulCount > 1) {
            int expectedArticles = result.getResults().stream()
                    .filter(r -> r.isSuccess())
                    .findFirst()
                    .get()
                    .getAantalArtikelen();

            result.getResults().stream()
                    .filter(r -> r.isSuccess())
                    .forEach(r -> assertEquals(expectedArticles, r.getAantalArtikelen(),
                            r.getLibrary() + " should find the same number of articles"));
        }
    }

    @Test
    void testPerformanceMetricsAreRecorded() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries(validXml);

        result.getResults().stream()
                .filter(r -> r.isSuccess())
                .forEach(r -> {
                    assertTrue(r.getTijdMs() >= 0, r.getLibrary() + " time should be recorded");
                    assertTrue(r.getGeheugenKb() >= 0, r.getLibrary() + " memory should be recorded");
                });
    }

    @Test
    void testGenerateLargeXml() {
        String largeXml = xmlComparisonService.generateLargeXml(10);

        assertNotNull(largeXml);
        assertFalse(largeXml.isEmpty());
        assertTrue(largeXml.startsWith("<?xml"), "Should start with XML declaration");
        assertTrue(largeXml.contains("<wet"), "Should contain wet element");
        assertTrue(largeXml.contains("<artikel"), "Should contain artikel elements");
        assertTrue(largeXml.contains("</wet>"), "Should end with closing wet tag");

        // Verify structure by counting articles
        int artikelCount = countOccurrences(largeXml, "<artikel id=");
        assertEquals(10, artikelCount, "Should contain 10 artikel elements");
    }

    @Test
    void testGenerateLargeXmlWithZero() {
        String xml = xmlComparisonService.generateLargeXml(0);

        assertNotNull(xml);
        assertTrue(xml.contains("<wet"), "Should contain wet element");
        assertTrue(xml.contains("</wet>"), "Should close wet element");

        int artikelCount = countOccurrences(xml, "<artikel");
        assertEquals(0, artikelCount, "Should contain 0 artikel elements");
    }

    @Test
    void testGenerateLargeXmlStructure() {
        String xml = xmlComparisonService.generateLargeXml(5);

        // Verify basic structure
        assertTrue(xml.contains("<metadata>"), "Should have metadata");
        assertTrue(xml.contains("<titel>"), "Should have titel");
        assertTrue(xml.contains("<datum>"), "Should have datum");
        assertTrue(xml.contains("<hoofdstuk"), "Should have hoofdstuk");
        assertTrue(xml.contains("<artikel"), "Should have artikel");
        assertTrue(xml.contains("<kop>"), "Should have kop");
        assertTrue(xml.contains("<lid"), "Should have lid");
    }

    @Test
    void testGenerateLargeXmlCanBeParsed() {
        String largeXml = xmlComparisonService.generateLargeXml(20);

        ComparisonResult result = xmlComparisonService.compareAllLibraries(largeXml);

        assertNotNull(result);

        // At least some parsers should successfully parse the generated XML
        long successfulParsers = result.getResults().stream()
                .filter(r -> r.isSuccess())
                .count();

        assertTrue(successfulParsers > 0, "At least one parser should successfully parse generated XML");

        // Verify article count
        result.getResults().stream()
                .filter(r -> r.isSuccess())
                .forEach(r -> assertEquals(20, r.getAantalArtikelen(),
                        r.getLibrary() + " should find all 20 articles"));
    }

    @Test
    void testEmptyXmlContent() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries("");

        assertNotNull(result);
        assertEquals(4, result.getResults().size());
    }

    @Test
    void testNullXmlContent() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries(null);

        assertNotNull(result);
        assertEquals(4, result.getResults().size());
    }

    @Test
    void testStatisticsCalculation() {
        ComparisonResult result = xmlComparisonService.compareAllLibraries(validXml);

        result.berekenStatistieken();

        assertNotNull(result.getSnelste());
        assertNotNull(result.getMinsteGeheugen());
        assertNotNull(result.getAanbeveling());

        // Snelste should contain library name and time
        assertTrue(result.getSnelste().contains("ms"), "Snelste should show time in ms");

        // Minste geheugen should contain library name and memory
        assertTrue(result.getMinsteGeheugen().contains("KB"), "Minste geheugen should show memory in KB");
    }

    // Helper method to count occurrences
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}