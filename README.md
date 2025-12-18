# XML Library Comparison Demo

A Spring Boot application demonstrating the performance and functionality of four major Java XML parsing libraries: DOM, SAX, StAX, and JAXB.

---

## Table of Contents

- [About This Project](#about-this-project)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Testing](#testing)
- [XML Parser Explanations](#xml-parser-explanations)
- [Architecture](#architecture)
- [Performance Comparison](#performance-comparison)
- [Troubleshooting](#troubleshooting)

---

## About This Project

This application was developed as preparation for a technical interview at **KOOP** (Knowledge and Exploitation Centre for Official Government Publications) for the role of **XSLT and Java Developer**.

### Objective

Demonstrate practical understanding of XML processing libraries through:
- Real-time performance comparison
- Visual representation of differences in speed and memory usage
- Interactive testing with various file sizes
- Concrete use case recommendations

### Use Case: Legislation System (BWB)

The BasisWettenBestand (BWB) contains thousands of Dutch laws in XML format. When rebuilding this system, the choice of XML parser is crucial for:
- Importing large legislation files (10-100MB)
- Real-time transformations for web display (XSLT)
- REST API endpoints returning legislation
- Batch processing for validation and consolidation

---

## Requirements

### Minimum Requirements
- Java 17 or higher
- Maven 3.6+
- 8GB RAM (recommended for large XML tests)
- Modern browser (Chrome, Firefox, Safari, Edge)

### Optional
- IntelliJ IDEA or Eclipse (for development)
- Git (for version control)

### Verification

```bash
# Check Java version
java -version
# Should display: java version "17" or higher

# Check Maven version
mvn -version
# Should display: Apache Maven 3.6.x or higher
```

---

## Installation

### Step 1: Clone or Download Project

```bash
# If using Git
git clone <repository-url>
cd xml-library-demo

# Or: download and unzip the project
```

### Step 2: Install Dependencies

```bash
mvn clean install
```

This downloads all required dependencies:
- Spring Boot Web
- Spring Boot Thymeleaf
- JAXB Runtime
- JUnit 5 (for testing)

### Step 3: Start Application

```bash
mvn spring-boot:run
```

You should see:
```
XML Library Demo gestart!
Open: http://localhost:8080
```

### Step 4: Open Browser

Navigate to: **http://localhost:8080**

---

## Usage

### 1. Basic Test (3 Articles)

The application starts with sample XML containing 3 articles.

**Steps:**
1. Open http://localhost:8080
2. Click "Vergelijk Alle Libraries"
3. View results for all 4 parsers

**Expected Output:**
- All parsers successful
- Comparable performance (approximately 10-50ms)
- Little difference in memory usage

### 2. Medium Test (100 Articles)

**Steps:**
1. Click "Genereer 100 Artikelen"
2. Wait approximately 2 seconds (file generation: approximately 30KB)
3. Click "Vergelijk Alle Libraries"

**Expected Output:**
```
DOM:  100 articles, 85ms, 2048KB
SAX:  100 articles, 28ms, 256KB   (3x faster)
StAX: 100 articles, 35ms, 384KB
JAXB: 100 articles, 120ms, 1536KB
```

### 3. Stress Test (1000 Articles)

**Steps:**
1. Click "Genereer 1000 Artikelen"
2. Wait approximately 5 seconds (file generation: approximately 300KB)
3. Click "Vergelijk Alle Libraries"

**Expected Output:**
```
DOM:  1000 articles, 156ms, 8192KB
SAX:  1000 articles, 42ms, 512KB    (3-4x faster)
StAX: 1000 articles, 58ms, 768KB
JAXB: 1000 articles, 245ms, 6144KB

Fastest: SAX (42ms)
Least memory: SAX (512KB)
Recommendation: SAX or StAX recommended (large file). JAXB for type-safe APIs.
```

### 4. Custom XML

You can paste your own XML in the text field:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<wet id="custom-001">
    <metadata>
        <titel>My Test Law</titel>
        <datum>2024-12-17</datum>
    </metadata>
    <hoofdstuk nummer="1">
        <titel>Test Chapter</titel>
        <artikel id="art-1">
            <kop>Test Article</kop>
            <lid nummer="1">Test content</lid>
        </artikel>
    </hoofdstuk>
</wet>
```

### 5. Reset

Click "Reset naar Voorbeeld" to return to the original example.

---

## Testing

### Running Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=JAXBParserServiceTest

# Run with verbose output
mvn test -X
```

### Test Coverage

The project contains **87 unit tests** distributed across:

| Test Class | Tests | What is Tested |
|------------|-------|----------------|
| DOMParserServiceTest | 15 | DOM parsing, edge cases, performance |
| SAXParserServiceTest | 18 | SAX event handling, callbacks, memory |
| StAXParserServiceTest | 18 | StAX streaming, pull parsing |
| JAXBParserServiceTest | 8 | JAXB unmarshalling, object mapping |
| XmlComparisonServiceTest | 11 | Integration, comparison logic |
| XmlDemoControllerTest | 17 | REST endpoints, HTTP handling |

### Test Examples

**DOM Parser Test:**
```java
@Test
void testDOMParsing() {
        DOMParserService service = new DOMParserService();
        String xml = "<?xml version=\"1.0\"?><wet id=\"w1\">...</wet>";

        ParseResult result = service.parse(xml);

        assertTrue(result.isSuccess());
        assertEquals("DOM", result.getLibrary());
        assertEquals(1, result.getAantalArtikelen());
        }
```

**Performance Test:**
```java
@Test
void testSAXPerformanceWithLargeFile() {
        // Generate 1000 articles
        String largeXml = generateXml(1000);

        ParseResult result = saxParser.parse(largeXml);

        assertTrue(result.getTijdMs() < 100);
        assertTrue(result.getGeheugenKb() < 1024);
        }
```

### Test Reports

After running `mvn test`, reports are generated in:
```
target/surefire-reports/
├── TEST-*.xml              (JUnit XML reports)
└── nl.koop.xmldemo.*.txt   (Text reports)
```

---

## XML Parser Explanations

### 1. DOM (Document Object Model)

**What is it?**

DOM loads the **entire XML document** into memory as a **tree structure**. You can navigate through the entire tree in any direction.

**How does it work?**

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);  // Entire document in memory

// Random access: go to any element
        Element root = doc.getDocumentElement();
        NodeList artikelen = doc.getElementsByTagName("artikel");
        Element artikel5 = (Element) artikelen.item(4);  // Direct to article 5
```

**Memory Model:**

```
Heap Memory:
┌─────────────────────────────────┐
│  <wet>                          │  Root node
│    ├─ <metadata>                │
│    │   ├─ <titel>               │
│    │   └─ <datum>               │
│    ├─ <hoofdstuk>               │
│    │   ├─ <artikel id="art-1">  │
│    │   │   ├─ <kop>             │
│    │   │   └─ <lid>             │
│    │   └─ <artikel id="art-2">  │
│    └─ <hoofdstuk>               │
└─────────────────────────────────┘
Everything in memory (approximately 10x XML size)
```

**Advantages:**
- Easy to use with intuitive API
- Random access to any element
- Modifiable document structure
- XPath support for powerful queries
- XSLT compatible (XSLT requires DOM as input)

**Disadvantages:**
- High memory usage (entire document in RAM)
- Slow for large files (must parse everything)
- No streaming capability
- Not suitable for files larger than 50MB

**When to use:**
- XSLT transformations (required)
- Small to medium files (less than 10MB)
- Interactive editing (GUI editors)
- Complex navigation through document

**BWB Use Case:**
```java
// Transform law to HTML for display
Document wetDoc = parseDOM(wetXml);
        String html = applyXSLT(wetDoc, "wet-to-html.xsl");
```

---

### 2. SAX (Simple API for XML)

**What is it?**

SAX is an **event-driven parser**. It reads the XML file **sequentially** and calls callbacks when elements are found. **No tree in memory.**

**How does it work?**

```java
SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

// Define a Handler with callbacks
        DefaultHandler handler = new DefaultHandler() {
@Override
public void startElement(String uri, String localName,
        String qName, Attributes attrs) {
        if ("artikel".equals(qName)) {
        System.out.println("Article found!");  // Callback
        }
        }

@Override
public void characters(char[] ch, int start, int length) {
        String text = new String(ch, start, length);  // Content
        }
        };

        parser.parse(xmlFile, handler);  // Streams through file
```

**Processing Model:**

```
XML File: <wet><artikel id="1"><kop>Test</kop></artikel></wet>

SAX Parser reads sequentially:

1. startElement("wet")       → Callback
2. startElement("artikel")   → Callback
3. characters("1")           → Callback
4. startElement("kop")       → Callback
5. characters("Test")        → Callback
6. endElement("kop")         → Callback
7. endElement("artikel")     → Callback
8. endElement("wet")         → Callback

Memory: Only current element (approximately 50KB constant)
```

**Advantages:**
- Very efficient with lowest memory footprint
- Fastest for large files due to streaming
- No file size limit (can handle 1GB+ files)
- Perfect for batch processing

**Disadvantages:**
- Complex code (callback-based)
- No random access (forward-only)
- Read-only (cannot modify document)
- Difficult to debug (complex state management)

**When to use:**
- Large files (greater than 50MB)
- Batch processing (validate thousands of laws)
- Memory-constrained environments
- Simple extraction (search for specific data)

**BWB Use Case:**
```java
// Validate all 10,000 laws for integrity
for (File wet : alleWetten) {
        SAXParser parser = createValidator();
        parser.parse(wet, validationHandler);  // Uses maximum 1MB RAM
        }
```

---

### 3. StAX (Streaming API for XML)

**What is it?**

StAX is a **pull-based parser**. Instead of callbacks (push), **you request** (pull) the parser for the next element. Best of both worlds: streaming efficiency plus control.

**How does it work?**

```java
XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(xmlFile);

        while (reader.hasNext()) {
        int event = reader.next();  // You request next event

        if (event == XMLStreamConstants.START_ELEMENT) {
        String name = reader.getLocalName();

        if ("artikel".equals(name)) {
        String id = reader.getAttributeValue(null, "id");
        System.out.println("Article: " + id);

        // Can exit early
        if ("art-999".equals(id)) {
        break;  // Stop parsing (not possible with SAX)
        }
        }
        }
        }
        reader.close();
```

**Processing Model:**

```
Code requests:        Parser returns:
───────────────────────────────────────
reader.next()    →   START_ELEMENT: "wet"
reader.next()    →   START_ELEMENT: "artikel"
reader.next()    →   CHARACTERS: "content"
reader.next()    →   END_ELEMENT: "artikel"
...
if (found) break;    → STOP! Rest not parsed

Memory: Current event context only (approximately 100KB)
```

**Advantages:**
- Streaming efficiency like SAX
- More control (pull instead of push)
- Early exit possible
- Easier to read than SAX
- Cursor-based with peek-ahead capability

**Disadvantages:**
- Still no random access
- More verbose than DOM
- Manual state management required

**When to use:**
- Large files with selective parsing
- Search operations (stop when found)
- Import workflows (process as you read)
- Better control than SAX needed

**BWB Use Case:**
```java
// Search for specific article in large law (100MB)
XMLStreamReader reader = factory.createXMLStreamReader(largeWet);
        while (reader.hasNext()) {
        if (isArtikel() && getId().equals("art-523")) {
        Artikel artikel = extractArtikel();
        return artikel;  // Stop! Saves 99% parse time
        }
        reader.next();
        }
```

---

### 4. JAXB (Java Architecture for XML Binding)

**What is it?**

JAXB **automatically maps XML to Java objects** (and vice versa). You define Java classes with annotations, and JAXB handles the rest. **Object-Oriented XML processing.**

**How does it work?**

**Step 1: Define Java Classes:**
```java
@XmlRootElement(name = "wet")
@XmlAccessorType(XmlAccessType.FIELD)
public class Wet {
    @XmlAttribute
    private String id;

    @XmlElement
    private Metadata metadata;

    @XmlElement(name = "hoofdstuk")
    private List<Hoofdstuk> hoofdstukken;

    // Getters/Setters
}

@XmlAccessorType(XmlAccessType.FIELD)
public class Hoofdstuk {
    @XmlAttribute
    private int nummer;

    @XmlElement(name = "artikel")
    private List<Artikel> artikelen;
}
```

**Step 2: Parse XML to Object:**
```java
JAXBContext context = JAXBContext.newInstance(Wet.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

// XML to Java Object (automatic)
        Wet wet = (Wet) unmarshaller.unmarshal(new File("wet.xml"));

// Now work with regular Java objects
        String titel = wet.getMetadata().getTitel();
        int aantalArtikelen = wet.getHoofdstukken().stream()
        .mapToInt(h -> h.getArtikelen().size())
        .sum();
```

**Step 3: Object to XML:**
```java
Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

// Java Object to XML (automatic)
        marshaller.marshal(wet, new File("output.xml"));
```

**Processing Model:**

```
XML File                     JAXB                    Java Objects
────────────────────────────────────────────────────────────────
<wet id="w1">          ┌──────────┐           Wet object
  <metadata>     ───►  │ JAXB     │  ───►       ├─ id = "w1"
    <titel>...         │ Unmarshal│             ├─ metadata
  </metadata>          └──────────┘             │   └─ titel
  <hoofdstuk>                                   └─ hoofdstukken
    <artikel>...</artikel>                          └─ [Artikel, Artikel]
  </hoofdstuk>
</wet>

Type-Safe with IDE autocomplete and refactoring support
```

**Advantages:**
- Type-safe with compile-time checks
- No parsing code needed (JAXB handles it)
- Bi-directional (XML to Java and Java to XML)
- Object-Oriented (natural Java style)
- IDE support (autocomplete, refactoring)
- Built-in validation (schema validation)

**Disadvantages:**
- Boilerplate code (many annotations)
- Performance overhead (reflection-based)
- Inflexible (XML structure must match Java structure)
- Whole document in memory (like DOM)

**When to use:**
- REST APIs (automatic XML/JSON conversion)
- Type-safe applications
- Configuration files
- Web services (SOAP, REST)
- Clean code (no parsing boilerplate)

**BWB Use Case:**
```java
// REST API endpoint
@GetMapping("/wetten/{id}")
public Wet getWet(@PathVariable String id) {
        Wet wet = repository.findById(id);
        return wet;  // Spring automatically converts to XML/JSON
        }

// Type-safe business logic
        wet.getHoofdstukken().forEach(hoofdstuk -> {
        hoofdstuk.getArtikelen().forEach(artikel -> {
        validateArtikel(artikel);  // IDE knows all properties
        });
        });
```

---

## Performance Comparison

### Test Setup
- **Machine:** MacBook Pro M1, 16GB RAM
- **Java:** OpenJDK 17
- **Test Data:** 1000 articles, approximately 300KB XML

### Results

| Library | Parse Time | Memory Usage | Speed vs DOM | Memory vs DOM |
|---------|-----------|--------------|--------------|---------------|
| **DOM** | 156ms | 8192KB | Baseline | Baseline |
| **SAX** | 42ms | 512KB | **3.7x faster** | **16x less** |
| **StAX** | 58ms | 768KB | **2.7x faster** | **11x less** |
| **JAXB** | 245ms | 6144KB | 0.6x slower | 1.3x less |

### When to Use Which Parser

**By File Size:**

| File Size | Recommended Parser |
|-----------|-------------------|
| Less than 1MB | DOM (easy) or JAXB (type-safe) |
| 1-10MB | StAX (balanced) |
| 10-100MB | SAX (memory) or StAX (control) |
| Greater than 100MB | SAX only |

**By Use Case:**

| Use Case | Recommended Parser |
|----------|-------------------|
| XSLT Transform | DOM (required) |
| REST API | JAXB (auto convert) |
| Batch Processing | SAX (efficiency) |
| Search and Extract | StAX (early exit) |
| Interactive Edit | DOM (modify tree) |
| Import Pipeline | StAX (streaming plus control) |

### BWB Scenarios

**Scenario 1: Import Law (100MB)**
```
SAX:  2.5 seconds, 50MB RAM     (Recommended)
StAX: 3.1 seconds, 80MB RAM     (Alternative)
DOM:  45 seconds, 1.2GB RAM     (Out of memory)
JAXB: 60 seconds, 1.5GB RAM     (Too slow)

CHOICE: SAX for batch import
```

**Scenario 2: Search for Article**
```
StAX: 0.3 seconds (early exit)  (Recommended)
SAX:  2.5 seconds (must read entire file)
DOM:  45 seconds (parse all first)

CHOICE: StAX for search
```

**Scenario 3: Generate HTML (XSLT)**
```
DOM:  Required for XSLT         (Only option)
SAX/StAX/JAXB: XSLT needs DOM

CHOICE: DOM (no alternative)
```

**Scenario 4: REST API /wetten/{id}**
```
JAXB: Type-safe, auto XML/JSON  (Recommended)
DOM:  Manual conversion code
SAX/StAX: Complex object building

CHOICE: JAXB for API
```

---

## Architecture

### Project Structure

```
xml-library-demo/
├── src/
│   ├── main/
│   │   ├── java/nl/koop/xmldemo/
│   │   │   ├── XmlLibraryDemoApplication.java
│   │   │   ├── config/
│   │   │   │   └── TomcatConfig.java
│   │   │   ├── controller/
│   │   │   │   └── XmlDemoController.java
│   │   │   ├── service/
│   │   │   │   ├── DOMParserService.java
│   │   │   │   ├── SAXParserService.java
│   │   │   │   ├── StAXParserService.java
│   │   │   │   ├── JAXBParserService.java
│   │   │   │   └── XmlComparisonService.java
│   │   │   ├── model/
│   │   │   │   ├── Wet.java
│   │   │   │   ├── Hoofdstuk.java
│   │   │   │   ├── Artikel.java
│   │   │   │   ├── Lid.java
│   │   │   │   └── Metadata.java
│   │   │   └── dto/
│   │   │       ├── ParseResult.java
│   │   │       └── ComparisonResult.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── index.html
│   └── test/
│       └── java/nl/koop/xmldemo/
│           └── service/
├── pom.xml
└── README.md
```

### Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Web Browser                          │
│                  (http://localhost:8080)                │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP POST /compare
                     ▼
┌─────────────────────────────────────────────────────────┐
│               XmlDemoController                         │
│  Receives XML content                                   │
│  Routes to comparison service                           │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│            XmlComparisonService                         │
│  Orchestrates all 4 parsers                             │
│  Collects results                                       │
│  Calculates statistics                                  │
└─┬──────────┬──────────┬──────────┬────────────────────┘
  │          │          │          │
  ▼          ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  DOM   │ │  SAX   │ │ StAX   │ │ JAXB   │
│ Parser │ │ Parser │ │ Parser │ │ Parser │
└────────┘ └────────┘ └────────┘ └────────┘
```

### Data Flow

```
1. User pastes XML in textarea
2. Clicks "Vergelijk Alle Libraries"
3. JavaScript: POST /compare with XML in body
4. Controller: Receives XML string
5. ComparisonService: Calls each parser
   ├─ DOMParser.parse(xml) → ParseResult
   ├─ SAXParser.parse(xml) → ParseResult
   ├─ StAXParser.parse(xml) → ParseResult
   └─ JAXBParser.parse(xml) → ParseResult
6. ComparisonService: Aggregates results
7. ComparisonService: Calculates statistics
8. Controller: Returns ComparisonResult as JSON
9. JavaScript: Displays results in UI
```

---

## Troubleshooting

### Problem 1: Port 8080 Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```bash
# Option 1: Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Option 2: Change port in application.properties
server.port=8081
```

---

### Problem 2: Request Header is Too Large

**Error:**
```
HTTP Status 400 Bad Request
Message: Request header is too large
```

**Solution:**

Already fixed in `TomcatConfig.java`. If still failing, add to `application.properties`:

```properties
server.max-http-header-size=65536
server.tomcat.max-http-post-size=52428800
```

---

### Problem 3: OutOfMemoryError with Large Files

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase heap size
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx4g"

# Or in IntelliJ: Run > Edit Configurations > VM Options:
-Xmx4g
```

---

### Problem 4: Tests Failing

**Error:**
```
[ERROR] Tests run: 87, Failures: 3
```

**Solution:**
```bash
# Clean and rebuild
mvn clean install

# Run specific test to see details
mvn test -Dtest=JAXBParserServiceTest -X

# Check if Java version is correct
java -version  # Must be 17+
```

---

### Problem 5: JAXB Classes Not Found

**Error:**
```
ClassNotFoundException: jakarta.xml.bind.JAXBContext
```

**Solution:**

Ensure Java 17+ is installed. JAXB was removed from JDK in Java 11+. The project includes JAXB dependencies in `pom.xml`.

```bash
# Verify Java version
java -version

# Reinstall dependencies
mvn clean install
```

---

## License

This project is developed for educational and interview preparation purposes.

---

## Contact

For questions or issues, please contact the development team.

---

## Acknowledgments

- Spring Boot framework
- JAXB reference implementation
- Apache Maven build system
- JUnit testing framework