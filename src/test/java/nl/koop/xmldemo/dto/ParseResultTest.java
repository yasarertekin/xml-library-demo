// ParseResultTest.java
package nl.koop.xmldemo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParseResultTest {

    private ParseResult parseResult;

    @BeforeEach
    void setUp() {
        parseResult = new ParseResult("DOM");
    }

    @Test
    void testConstructor() {
        assertEquals("DOM", parseResult.getLibrary());
        assertTrue(parseResult.isSuccess());
    }

    @Test
    void testSetAndGetLibrary() {
        parseResult.setLibrary("SAX");
        assertEquals("SAX", parseResult.getLibrary());
    }

    @Test
    void testSetAndGetAantalArtikelen() {
        parseResult.setAantalArtikelen(42);
        assertEquals(42, parseResult.getAantalArtikelen());
    }

    @Test
    void testSetAndGetTijdMs() {
        parseResult.setTijdMs(150L);
        assertEquals(150L, parseResult.getTijdMs());
    }

    @Test
    void testSetAndGetGeheugenKb() {
        parseResult.setGeheugenKb(1024L);
        assertEquals(1024L, parseResult.getGeheugenKb());
    }

    @Test
    void testSetAndGetSuccess() {
        assertTrue(parseResult.isSuccess());

        parseResult.setSuccess(false);
        assertFalse(parseResult.isSuccess());
    }

    @Test
    void testSetError() {
        String error = "Parse error occurred";
        parseResult.setError(error);

        assertEquals(error, parseResult.getError());
        assertFalse(parseResult.isSuccess(), "Setting error should set success to false");
    }

    @Test
    void testAddArtikelDetail() {
        parseResult.addArtikelDetail("Artikel 1");
        parseResult.addArtikelDetail("Artikel 2");

        List<String> details = parseResult.getArtikelDetails();
        assertNotNull(details);
        assertEquals(2, details.size());
        assertEquals("Artikel 1", details.get(0));
        assertEquals("Artikel 2", details.get(1));
    }

    @Test
    void testSetArtikelDetails() {
        List<String> details = List.of("Detail 1", "Detail 2", "Detail 3");
        parseResult.setArtikelDetails(details);

        assertEquals(3, parseResult.getArtikelDetails().size());
        assertEquals("Detail 1", parseResult.getArtikelDetails().get(0));
    }

    @Test
    void testSuccessfulResult() {
        parseResult.setLibrary("SAX");
        parseResult.setAantalArtikelen(10);
        parseResult.setTijdMs(75L);
        parseResult.setGeheugenKb(256L);
        parseResult.addArtikelDetail("Detail 1");

        assertEquals("SAX", parseResult.getLibrary());
        assertTrue(parseResult.isSuccess());
        assertEquals(10, parseResult.getAantalArtikelen());
        assertEquals(75L, parseResult.getTijdMs());
        assertEquals(256L, parseResult.getGeheugenKb());
        assertNotNull(parseResult.getArtikelDetails());
        assertNull(parseResult.getError());
    }

    @Test
    void testFailedResult() {
        parseResult.setError("XML parsing failed");

        assertEquals("DOM", parseResult.getLibrary());
        assertFalse(parseResult.isSuccess());
        assertEquals("XML parsing failed", parseResult.getError());
    }

    @Test
    void testDefaultValues() {
        ParseResult newResult = new ParseResult("Test");

        assertEquals("Test", newResult.getLibrary());
        assertTrue(newResult.isSuccess());
        assertEquals(0, newResult.getAantalArtikelen());
        assertEquals(0L, newResult.getTijdMs());
        assertEquals(0L, newResult.getGeheugenKb());
        assertNotNull(newResult.getArtikelDetails());
        assertTrue(newResult.getArtikelDetails().isEmpty());
        assertNull(newResult.getError());
    }

    @Test
    void testZeroValues() {
        parseResult.setAantalArtikelen(0);
        parseResult.setTijdMs(0L);
        parseResult.setGeheugenKb(0L);

        assertEquals(0, parseResult.getAantalArtikelen());
        assertEquals(0L, parseResult.getTijdMs());
        assertEquals(0L, parseResult.getGeheugenKb());
    }

    @Test
    void testLargeNumbers() {
        parseResult.setAantalArtikelen(1000000);
        parseResult.setTijdMs(999999L);
        parseResult.setGeheugenKb(10240000L);

        assertEquals(1000000, parseResult.getAantalArtikelen());
        assertEquals(999999L, parseResult.getTijdMs());
        assertEquals(10240000L, parseResult.getGeheugenKb());
    }

    @Test
    void testMultipleArtikelDetails() {
        for (int i = 1; i <= 10; i++) {
            parseResult.addArtikelDetail("Artikel " + i);
        }

        assertEquals(10, parseResult.getArtikelDetails().size());
        assertEquals("Artikel 1", parseResult.getArtikelDetails().get(0));
        assertEquals("Artikel 10", parseResult.getArtikelDetails().get(9));
    }

    @Test
    void testSpecialCharactersInError() {
        String errorWithSpecialChars = "Error: <XML> parsing failed with & character";
        parseResult.setError(errorWithSpecialChars);
        assertEquals(errorWithSpecialChars, parseResult.getError());
    }

    @Test
    void testEmptyArtikelDetails() {
        assertNotNull(parseResult.getArtikelDetails());
        assertTrue(parseResult.getArtikelDetails().isEmpty());
    }

    @Test
    void testSetSuccessAfterError() {
        parseResult.setError("Error occurred");
        assertFalse(parseResult.isSuccess());

        parseResult.setSuccess(true);
        assertTrue(parseResult.isSuccess());
        // Error should still be present
        assertNotNull(parseResult.getError());
    }
}