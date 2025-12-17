package nl.koop.xmldemo.controller;

import nl.koop.xmldemo.dto.ComparisonResult;
import nl.koop.xmldemo.service.XmlComparisonService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class XmlDemoController {

    private final XmlComparisonService comparisonService;

    public XmlDemoController(XmlComparisonService comparisonService) {
        this.comparisonService = comparisonService;
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