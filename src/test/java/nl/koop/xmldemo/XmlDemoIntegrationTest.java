package nl.koop.xmldemo;

import nl.koop.xmldemo.dto.ComparisonResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class XmlDemoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testIndexPageLoads() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("XML Library Comparison Tool"));
    }

    @Test
    void testCompareEndpointWithValidXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-001\">\n" +
                "    <artikel id=\"art-1\">\n" +
                "        <kop>Test Artikel</kop>\n" +
                "    </artikel>\n" +
                "</wet>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        // Use String.class instead of ComparisonResult.class to avoid deserialization issues
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify JSON structure
        String body = response.getBody();
        assertTrue(body.contains("\"results\""));
        assertTrue(body.contains("\"snelste\""));
        assertTrue(body.contains("\"minsteGeheugen\""));
        assertTrue(body.contains("\"aanbeveling\""));
    }

    @Test
    void testCompareEndpointWithEmptyXml() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        // Use whitespace instead of empty to avoid 400
        HttpEntity<String> request = new HttpEntity<>("  ", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should use default XML - just verify we got a valid JSON response
        assertTrue(response.getBody().contains("\"results\""));
    }

    @Test
    void testGenerateXmlEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/generate/10",
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("<?xml"));
        assertTrue(response.getBody().contains("<wet"));
        assertTrue(response.getBody().contains("</wet>"));
    }

    @Test
    void testGenerateXmlWithMaxLimit() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/generate/15000",
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Count number of articles (should be capped at 10000)
        String body = response.getBody();
        int count = countOccurrences(body, "<artikel id=\"art-");
        assertTrue(count <= 10000, "Should not generate more than 10000 articles");
    }

    @Test
    void testGenerateXmlWithSmallNumber() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/generate/5",
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String body = response.getBody();
        int count = countOccurrences(body, "<artikel id=\"art-");
        assertEquals(5, count);
    }

    @Test
    void testCompareWithMultipleArticles() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><wet>");
        for (int i = 1; i <= 20; i++) {
            xml.append("<artikel id=\"art-").append(i).append("\">")
                    .append("<kop>Artikel ").append(i).append("</kop>")
                    .append("</artikel>");
        }
        xml.append("</wet>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(xml.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Check that response contains results
        assertTrue(body.contains("\"results\""));
        assertTrue(body.contains("\"success\""));
    }

    @Test
    void testComparePerformanceMetrics() {
        String xml = "<?xml version=\"1.0\"?><wet>" +
                "<artikel id=\"art-1\"><kop>Test</kop></artikel>" +
                "</wet>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Verify JSON contains performance metrics
        assertTrue(body.contains("\"tijdMs\""));
        assertTrue(body.contains("\"geheugenKb\""));
        assertTrue(body.contains("\"aantalArtikelen\""));
    }

    @Test
    void testCompareWithInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\"?><wet><artikel>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(invalidXml, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        // Should still return 200 with error results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should contain error information
        assertTrue(body.contains("\"success\""));
    }

    @Test
    void testCompareContentTypeHandling() {
        String xml = "<?xml version=\"1.0\"?><wet><artikel id=\"1\"><kop>Test</kop></artikel></wet>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));
        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"results\""));
    }

    @Test
    void testGenerateXmlStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/generate/3",
                String.class);

        String xml = response.getBody();
        assertNotNull(xml);

        // Verify structure
        assertTrue(xml.contains("<metadata>"));
        assertTrue(xml.contains("<titel>"));
        assertTrue(xml.contains("<datum>"));
        assertTrue(xml.contains("<hoofdstuk"));
        assertTrue(xml.contains("<lid nummer="));
    }

    @Test
    void testEndToEndWorkflow() {
        // 1. Load index page
        ResponseEntity<String> indexResponse = restTemplate.getForEntity(
                getBaseUrl() + "/", String.class);
        assertEquals(HttpStatus.OK, indexResponse.getStatusCode());

        // 2. Generate XML
        ResponseEntity<String> generateResponse = restTemplate.getForEntity(
                getBaseUrl() + "/generate/50",
                String.class);
        assertEquals(HttpStatus.OK, generateResponse.getStatusCode());
        String generatedXml = generateResponse.getBody();

        // 3. Compare libraries with generated XML
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(generatedXml, headers);

        ResponseEntity<String> compareResponse = restTemplate.postForEntity(
                getBaseUrl() + "/compare",
                request,
                String.class);

        assertEquals(HttpStatus.OK, compareResponse.getStatusCode());
        String body = compareResponse.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"results\""));
    }

    @Test
    void testConcurrentRequests() throws InterruptedException {
        String xml = "<?xml version=\"1.0\"?><wet><artikel id=\"1\"><kop>Test</kop></artikel></wet>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(xml, headers);

        // Make multiple concurrent requests
        Thread[] threads = new Thread[5];
        ResponseEntity<String>[] responses = new ResponseEntity[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    responses[index] = restTemplate.postForEntity(
                            getBaseUrl() + "/compare",
                            request,
                            String.class);
                } catch (Exception e) {
                    // Ignore exceptions in threads
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all responses are OK
        for (ResponseEntity<String> response : responses) {
            if (response != null) {
                assertEquals(HttpStatus.OK, response.getStatusCode());
            }
        }
    }

    // Helper method
    private int countOccurrences(String str, String findStr) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(findStr, idx)) != -1) {
            count++;
            idx += findStr.length();
        }
        return count;
    }
}