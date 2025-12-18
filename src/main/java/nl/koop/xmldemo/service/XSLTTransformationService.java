package nl.koop.xmldemo.service;


import nl.koop.xmldemo.dto.TransformResult;
import nl.koop.xmldemo.model.Wet;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.time.LocalDateTime;

@Service
public class XSLTTransformationService {

    private static final String XSLT_STYLESHEET = createXSLTStylesheet();

    /**
     * Transform using DOM (Recommended for XSLT)
     */
    public TransformResult transformUsingDOM(String xmlContent) {
        TransformResult result = new TransformResult("DOM");

        try {
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Step 1: Parse XML to DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                    new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))
            );

            // Step 2: Apply XSLT transformation
            String html = applyXSLT(new DOMSource(doc));
            result.setOutput(html);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setError("DOM XSLT error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Transform using SAX (Memory efficient)
     */
    public TransformResult transformUsingSAX(String xmlContent) {
        TransformResult result = new TransformResult("SAX");

        try {
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Create SAX source
            InputSource inputSource = new InputSource(new StringReader(xmlContent));
            SAXSource saxSource = new SAXSource(inputSource);

            // Apply XSLT
            String html = applyXSLT(saxSource);
            result.setOutput(html);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setError("SAX XSLT error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Transform using StAX
     */
    public TransformResult transformUsingStAX(String xmlContent) {
        TransformResult result = new TransformResult("StAX");
        XMLStreamReader reader = null;

        try {
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Create StAX reader
            XMLInputFactory factory = XMLInputFactory.newInstance();
            reader = factory.createXMLStreamReader(new StringReader(xmlContent));
            StAXSource staxSource = new StAXSource(reader);

            // Apply XSLT
            String html = applyXSLT(staxSource);
            result.setOutput(html);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setError("StAX XSLT error: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {}
            }
        }

        return result;
    }

    /**
     * Transform using JAXB (must parse to JAXB object first)
     */
    public TransformResult transformUsingJAXB(String xmlContent) {
        TransformResult result = new TransformResult("JAXB");

        try {
            long startTime = System.nanoTime();
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Step 1: Parse to JAXB object
            JAXBContext context = JAXBContext.newInstance(Wet.class);
            Wet wet = (Wet) context.createUnmarshaller()
                    .unmarshal(new StringReader(xmlContent));

            // Step 2: Marshal JAXB to DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().newDocument();

            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(wet, doc);

            // Step 3: Apply XSLT to DOM
            String html = applyXSLT(new DOMSource(doc));
            result.setOutput(html);

            long endTime = System.nanoTime();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            result.setTijdMs((endTime - startTime) / 1_000_000);
            result.setGeheugenKb((memAfter - memBefore) / 1024);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setError("JAXB XSLT error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Common method to apply XSLT transformation
     */
    private String applyXSLT(Source source) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();

        // Create transformer from embedded stylesheet
        StreamSource xsltSource = new StreamSource(new StringReader(XSLT_STYLESHEET));
        Transformer transformer = factory.newTransformer(xsltSource);

        // Set parameters
        transformer.setParameter("timestamp", LocalDateTime.now().toString());
        transformer.setParameter("generatedBy", "XML Library Demo");

        // Transform
        StringWriter output = new StringWriter();
        transformer.transform(source, new StreamResult(output));

        return output.toString();
    }

    /**
     * Create embedded XSLT stylesheet
     */
    private static String createXSLTStylesheet() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>
    
    <xsl:param name="timestamp"/>
    <xsl:param name="generatedBy"/>
    
    <xsl:template match="/wet">
        <html>
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title><xsl:value-of select="metadata/titel"/></title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        max-width: 900px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        margin: 0 0 10px 0;
                        font-size: 2em;
                    }
                    .metadata {
                        opacity: 0.9;
                        font-size: 0.9em;
                    }
                    .hoofdstuk {
                        background: white;
                        padding: 25px;
                        margin-bottom: 20px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .hoofdstuk-titel {
                        color: #667eea;
                        font-size: 1.5em;
                        margin: 0 0 20px 0;
                        border-bottom: 2px solid #667eea;
                        padding-bottom: 10px;
                    }
                    .artikel {
                        margin: 20px 0;
                        padding: 15px;
                        background: #f9f9f9;
                        border-left: 4px solid #764ba2;
                        border-radius: 5px;
                    }
                    .artikel-kop {
                        font-weight: bold;
                        color: #333;
                        margin-bottom: 10px;
                        font-size: 1.1em;
                    }
                    .lid {
                        margin: 10px 0 10px 20px;
                        line-height: 1.6;
                        color: #555;
                    }
                    .lid-nummer {
                        font-weight: bold;
                        color: #667eea;
                        margin-right: 5px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 40px;
                        padding: 20px;
                        color: #666;
                        font-size: 0.9em;
                        border-top: 1px solid #ddd;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1><xsl:value-of select="metadata/titel"/></h1>
                    <div class="metadata">
                        <p>Datum: <xsl:value-of select="metadata/datum"/></p>
                        <p>Wet ID: <xsl:value-of select="@id"/></p>
                    </div>
                </div>
                
                <xsl:apply-templates select="hoofdstuk"/>
                
                <div class="footer">
                    <p>Gegenereerd door: <xsl:value-of select="$generatedBy"/></p>
                    <p>Tijdstip: <xsl:value-of select="$timestamp"/></p>
                </div>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="hoofdstuk">
        <div class="hoofdstuk">
            <h2 class="hoofdstuk-titel">
                Hoofdstuk <xsl:value-of select="@nummer"/>: 
                <xsl:value-of select="titel"/>
            </h2>
            <xsl:apply-templates select="artikel"/>
        </div>
    </xsl:template>
    
    <xsl:template match="artikel">
        <div class="artikel">
            <div class="artikel-kop">
                <xsl:value-of select="kop"/>
            </div>
            <xsl:apply-templates select="lid"/>
        </div>
    </xsl:template>
    
    <xsl:template match="lid">
        <div class="lid">
            <span class="lid-nummer"><xsl:value-of select="@nummer"/>.</span>
            <xsl:value-of select="."/>
        </div>
    </xsl:template>
    
</xsl:stylesheet>
                """;
    }
}