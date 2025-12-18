package nl.koop.xmldemo.controller;

import nl.koop.xmldemo.dto.ComparisonResult;
import nl.koop.xmldemo.dto.TransformResult;
import nl.koop.xmldemo.service.XmlComparisonService;
import nl.koop.xmldemo.service.XSLTTransformationService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class XmlDemoController {

    private final XmlComparisonService comparisonService;
    private final XSLTTransformationService xsltService;

    public XmlDemoController(XmlComparisonService comparisonService,
                             XSLTTransformationService xsltService) {
        this.comparisonService = comparisonService;
        this.xsltService = xsltService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("voorbeeldXml", getVoorbeeldXml());
        return "index";
    }

    @PostMapping(value = "/compare",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ComparisonResult compare(@RequestBody(required = false) String xmlContent) {
        System.out.println("Received XML content length: " +
                (xmlContent != null ? xmlContent.length() : 0) + " characters");

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            xmlContent = getVoorbeeldXml();
        }

        return comparisonService.compareAllLibraries(xmlContent);
    }

    /**
     * NEW: Transform XML to HTML using specific parser
     */
    @PostMapping(value = "/transform/{parser}",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TransformResult transform(@PathVariable String parser,
                                     @RequestBody(required = false) String xmlContent) {
        System.out.println("XSLT Transform request using: " + parser);

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            xmlContent = getVoorbeeldXml();
        }

        return switch (parser.toUpperCase()) {
            case "DOM" -> xsltService.transformUsingDOM(xmlContent);
            case "SAX" -> xsltService.transformUsingSAX(xmlContent);
            case "STAX" -> xsltService.transformUsingStAX(xmlContent);
            case "JAXB" -> xsltService.transformUsingJAXB(xmlContent);
            default -> {
                TransformResult error = new TransformResult(parser);
                error.setError("Unknown parser: " + parser);
                yield error;
            }
        };
    }

    /**
     * NEW: Preview transformed HTML
     */
    @GetMapping("/preview")
    public String preview(@RequestParam(defaultValue = "DOM") String parser,
                          @RequestParam(required = false) String xml,
                          Model model) {

        String xmlContent = (xml != null && !xml.trim().isEmpty())
                ? xml
                : getVoorbeeldXml();

        TransformResult result = switch (parser.toUpperCase()) {
            case "DOM" -> xsltService.transformUsingDOM(xmlContent);
            case "SAX" -> xsltService.transformUsingSAX(xmlContent);
            case "STAX" -> xsltService.transformUsingStAX(xmlContent);
            case "JAXB" -> xsltService.transformUsingJAXB(xmlContent);
            default -> xsltService.transformUsingDOM(xmlContent);
        };

        model.addAttribute("html", result.isSuccess() ? result.getOutput() : "");
        model.addAttribute("error", result.getError());
        model.addAttribute("parser", parser);
        model.addAttribute("tijdMs", result.getTijdMs());
        model.addAttribute("geheugenKb", result.getGeheugenKb());

        return "preview";
    }

    @GetMapping("/generate/{aantal}")
    @ResponseBody
    public String generateLargeXml(@PathVariable int aantal) {
        if (aantal > 10000) {
            aantal = 10000;
        }
        System.out.println("Generating XML with " + aantal + " articles");
        return comparisonService.generateLargeXml(aantal);
    }

    private String getVoorbeeldXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wet id=\"wet-001\">\n" +
                "    <metadata>\n" +
                "        <titel>Voorbeeldwet Educatie</titel>\n" +
                "        <datum>2024-01-15</datum>\n" +
                "    </metadata>\n" +
                "    <hoofdstuk nummer=\"1\">\n" +
                "        <titel>Algemene Bepalingen</titel>\n" +
                "        <artikel id=\"art-1\">\n" +
                "            <kop>Artikel 1 - Definities</kop>\n" +
                "            <lid nummer=\"1\">In deze wet wordt verstaan onder student: een persoon ingeschreven aan een onderwijsinstelling.</lid>\n" +
                "            <lid nummer=\"2\">Onder docent wordt verstaan: een gekwalificeerd persoon belast met onderwijs.</lid>\n" +
                "        </artikel>\n" +
                "        <artikel id=\"art-2\">\n" +
                "            <kop>Artikel 2 - Toepassingsgebied</kop>\n" +
                "            <lid nummer=\"1\">Deze wet is van toepassing op alle onderwijsinstellingen.</lid>\n" +
                "        </artikel>\n" +
                "    </hoofdstuk>\n" +
                "    <hoofdstuk nummer=\"2\">\n" +
                "        <titel>Rechten en Plichten</titel>\n" +
                "        <artikel id=\"art-3\">\n" +
                "            <kop>Artikel 3 - Rechten</kop>\n" +
                "            <lid nummer=\"1\">Studenten hebben recht op kwaliteitsvol onderwijs.</lid>\n" +
                "        </artikel>\n" +
                "    </hoofdstuk>\n" +
                "</wet>";
    }
}