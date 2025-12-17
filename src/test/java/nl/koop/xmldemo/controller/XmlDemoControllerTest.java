package nl.koop.xmldemo.controller;

import nl.koop.xmldemo.dto.ComparisonResult;
import nl.koop.xmldemo.dto.ParseResult;
import nl.koop.xmldemo.service.XmlComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class XmlDemoControllerTest {

    @Mock
    private XmlComparisonService comparisonService;

    @InjectMocks
    private XmlDemoController controller;

    private MockMvc mockMvc;

    private String testXml;
    private ComparisonResult mockComparisonResult;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-001\">\n" +
                "    <artikel id=\"art-1\">\n" +
                "        <kop>Test Artikel</kop>\n" +
                "    </artikel>\n" +
                "</wet>";

        // Create mock comparison result
        mockComparisonResult = new ComparisonResult();

        ParseResult domResult = new ParseResult("DOM");
        domResult.setAantalArtikelen(5);
        domResult.setTijdMs(100L);
        domResult.setGeheugenKb(512L);
        domResult.addArtikelDetail("Artikel 1");
        domResult.addArtikelDetail("Artikel 2");

        ParseResult saxResult = new ParseResult("SAX");
        saxResult.setAantalArtikelen(5);
        saxResult.setTijdMs(50L);
        saxResult.setGeheugenKb(256L);
        saxResult.addArtikelDetail("Artikel 1");
        saxResult.addArtikelDetail("Artikel 2");

        mockComparisonResult.addResult(domResult);
        mockComparisonResult.addResult(saxResult);
        mockComparisonResult.berekenStatistieken();
    }

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("voorbeeldXml"))
                .andExpect(model().attribute("voorbeeldXml", containsString("<?xml version")));
    }

    @Test
    void testCompareWithValidXml() throws Exception {
        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(testXml))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results", hasSize(2)))
                .andExpect(jsonPath("$.results[0].library", is("DOM")))
                .andExpect(jsonPath("$.results[0].success", is(true)))
                .andExpect(jsonPath("$.results[0].aantalArtikelen", is(5)))
                .andExpect(jsonPath("$.results[1].library", is("SAX")))
                .andExpect(jsonPath("$.snelste", containsString("SAX")))
                .andExpect(jsonPath("$.minsteGeheugen", containsString("SAX")));

        verify(comparisonService, times(1)).compareAllLibraries(testXml);
    }

    @Test
    void testCompareWithEmptyXml() throws Exception {
        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        // Note: This test assumes controller has @RequestBody(required = false)
        // If not, change to .andExpect(status().is4xxClientError())
        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("  \n  "))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify that default XML is used when empty/whitespace
        verify(comparisonService, times(1)).compareAllLibraries(argThat(xml ->
                xml != null && xml.contains("Voorbeeldwet Educatie")));
    }

    @Test
    void testCompareWithWhitespaceXml() throws Exception {
        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("   "))
                .andExpect(status().isOk());

        // Verify that default XML is used when whitespace only
        verify(comparisonService, times(1)).compareAllLibraries(argThat(xml ->
                xml != null && xml.contains("Voorbeeldwet Educatie")));
    }

    @Test
    void testGenerateLargeXml() throws Exception {
        String generatedXml = "<?xml version=\"1.0\"?><wet><artikel id=\"1\"/></wet>";
        when(comparisonService.generateLargeXml(100))
                .thenReturn(generatedXml);

        mockMvc.perform(get("/generate/100"))
                .andExpect(status().isOk())
                .andExpect(content().string(generatedXml));

        verify(comparisonService, times(1)).generateLargeXml(100);
    }

    @Test
    void testGenerateLargeXmlWithMaxLimit() throws Exception {
        String generatedXml = "<?xml version=\"1.0\"?><wet><artikel id=\"1\"/></wet>";
        when(comparisonService.generateLargeXml(10000))
                .thenReturn(generatedXml);

        // Request 15000 but should be capped at 10000
        mockMvc.perform(get("/generate/15000"))
                .andExpect(status().isOk());

        verify(comparisonService, times(1)).generateLargeXml(10000);
    }

    @Test
    void testGenerateLargeXmlWithSmallNumber() throws Exception {
        String generatedXml = "<?xml version=\"1.0\"?><wet><artikel id=\"1\"/></wet>";
        when(comparisonService.generateLargeXml(10))
                .thenReturn(generatedXml);

        mockMvc.perform(get("/generate/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(generatedXml));

        verify(comparisonService, times(1)).generateLargeXml(10);
    }

    @Test
    void testCompareWithLargeXml() throws Exception {
        StringBuilder largeXml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 0; i < 100; i++) {
            largeXml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        largeXml.append("</wet>");

        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(largeXml.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(comparisonService, times(1)).compareAllLibraries(largeXml.toString());
    }

    @Test
    void testCompareWithSpecialCharacters() throws Exception {
        String xmlWithSpecialChars = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet><artikel><kop>Test &amp; Special €</kop></artikel></wet>";

        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
                        .content(xmlWithSpecialChars))
                .andExpect(status().isOk());

        verify(comparisonService, times(1)).compareAllLibraries(xmlWithSpecialChars);
    }

    @Test
    void testCompareReturnsCorrectJsonStructure() throws Exception {
        when(comparisonService.compareAllLibraries(anyString()))
                .thenReturn(mockComparisonResult);

        mockMvc.perform(post("/compare")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(testXml))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.snelste").exists())
                .andExpect(jsonPath("$.minsteGeheugen").exists())
                .andExpect(jsonPath("$.aanbeveling").exists());
    }
}
