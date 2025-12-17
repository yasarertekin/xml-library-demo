// ComparisonResultTest.java
package nl.koop.xmldemo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonResultTest {

    private ComparisonResult comparisonResult;
    private ParseResult result1;
    private ParseResult result2;

    @BeforeEach
    void setUp() {
        comparisonResult = new ComparisonResult();

        result1 = new ParseResult("DOM");
        result1.setAantalArtikelen(5);
        result1.setTijdMs(100L);
        result1.setGeheugenKb(512L);

        result2 = new ParseResult("SAX");
        result2.setAantalArtikelen(5);
        result2.setTijdMs(50L);
        result2.setGeheugenKb(256L);
    }

    @Test
    void testAddResult() {
        comparisonResult.addResult(result1);
        comparisonResult.addResult(result2);

        assertNotNull(comparisonResult.getResults());
        assertEquals(2, comparisonResult.getResults().size());
        assertEquals("DOM", comparisonResult.getResults().get(0).getLibrary());
        assertEquals("SAX", comparisonResult.getResults().get(1).getLibrary());
    }

    @Test
    void testBerekenStatistiekenWithSuccessfulResults() {
        comparisonResult.addResult(result1);
        comparisonResult.addResult(result2);
        comparisonResult.berekenStatistieken();

        assertNotNull(comparisonResult.getSnelste());
        assertNotNull(comparisonResult.getMinsteGeheugen());
        assertNotNull(comparisonResult.getAanbeveling());

        assertTrue(comparisonResult.getSnelste().contains("SAX"));
        assertTrue(comparisonResult.getSnelste().contains("50ms"));
        assertTrue(comparisonResult.getMinsteGeheugen().contains("SAX"));
        assertTrue(comparisonResult.getMinsteGeheugen().contains("256KB"));
    }

    @Test
    void testBerekenStatistiekenWithFailedResults() {
        ParseResult failed1 = new ParseResult("DOM");
        failed1.setError("Parse error");

        ParseResult failed2 = new ParseResult("SAX");
        failed2.setError("Parse error");

        comparisonResult.addResult(failed1);
        comparisonResult.addResult(failed2);
        comparisonResult.berekenStatistieken();

        assertNull(comparisonResult.getSnelste());
        assertNull(comparisonResult.getMinsteGeheugen());
    }

    @Test
    void testBerekenStatistiekenWithMixedResults() {
        ParseResult failed = new ParseResult("JAXB");
        failed.setError("Parse error");

        comparisonResult.addResult(result1);
        comparisonResult.addResult(result2);
        comparisonResult.addResult(failed);
        comparisonResult.berekenStatistieken();

        assertNotNull(comparisonResult.getSnelste());
        assertNotNull(comparisonResult.getMinsteGeheugen());
        assertTrue(comparisonResult.getSnelste().contains("SAX"));
    }

    @Test
    void testBerekenStatistiekenEmpty() {
        comparisonResult.berekenStatistieken();

        assertNull(comparisonResult.getSnelste());
        assertNull(comparisonResult.getMinsteGeheugen());
    }

    @Test
    void testAanbevelingForSmallFiles() {
        // Small files (average time < 100ms)
        result1.setTijdMs(50L);
        result2.setTijdMs(30L);

        comparisonResult.addResult(result1);
        comparisonResult.addResult(result2);
        comparisonResult.berekenStatistieken();

        assertNotNull(comparisonResult.getAanbeveling());
        assertTrue(comparisonResult.getAanbeveling().contains("DOM"));
        assertTrue(comparisonResult.getAanbeveling().contains("JAXB"));
    }

    @Test
    void testAanbevelingForLargeFiles() {
        // Large files (average time > 100ms)
        result1.setTijdMs(200L);
        result2.setTijdMs(150L);

        comparisonResult.addResult(result1);
        comparisonResult.addResult(result2);
        comparisonResult.berekenStatistieken();

        assertNotNull(comparisonResult.getAanbeveling());
        assertTrue(comparisonResult.getAanbeveling().contains("SAX") ||
                comparisonResult.getAanbeveling().contains("StAX"));
        assertTrue(comparisonResult.getAanbeveling().contains("JAXB"));
    }

    @Test
    void testGetResults() {
        comparisonResult.addResult(result1);

        assertNotNull(comparisonResult.getResults());
        assertFalse(comparisonResult.getResults().isEmpty());
        assertEquals(1, comparisonResult.getResults().size());
    }

    @Test
    void testMultipleResultsWithSamePerformance() {
        ParseResult result3 = new ParseResult("StAX");
        result3.setTijdMs(50L);
        result3.setGeheugenKb(256L);

        comparisonResult.addResult(result2);
        comparisonResult.addResult(result3);
        comparisonResult.berekenStatistieken();

        // Should pick one of them
        assertNotNull(comparisonResult.getSnelste());
        assertNotNull(comparisonResult.getMinsteGeheugen());
    }
}
