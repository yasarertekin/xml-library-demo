package nl.koop.xmldemo.dto;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResult {
    private List<ParseResult> results = new ArrayList<>();
    private String snelste;
    private String minsteGeheugen;
    private String aanbeveling;

    public void addResult(ParseResult result) {
        results.add(result);
    }

    public void berekenStatistieken() {
        if (results.isEmpty()) return;

        ParseResult snelsteResult = results.stream()
                .filter(ParseResult::isSuccess)
                .min((r1, r2) -> Long.compare(r1.getTijdMs(), r2.getTijdMs()))
                .orElse(null);

        ParseResult minsteGeheugenResult = results.stream()
                .filter(ParseResult::isSuccess)
                .min((r1, r2) -> Long.compare(r1.getGeheugenKb(), r2.getGeheugenKb()))
                .orElse(null);

        if (snelsteResult != null) {
            this.snelste = snelsteResult.getLibrary() + " (" + snelsteResult.getTijdMs() + "ms)";
        }

        if (minsteGeheugenResult != null) {
            this.minsteGeheugen = minsteGeheugenResult.getLibrary() + " (" + minsteGeheugenResult.getGeheugenKb() + "KB)";
        }

        bepaalAanbeveling();
    }

    private void bepaalAanbeveling() {
        StringBuilder sb = new StringBuilder();
        sb.append("Voor dit bestand: ");

        long gemiddeldeTijd = results.stream()
                .filter(ParseResult::isSuccess)
                .mapToLong(ParseResult::getTijdMs)
                .sum() / Math.max(1, results.size());

        if (gemiddeldeTijd > 100) {
            sb.append("SAX of StAX aanbevolen (groot bestand). ");
        } else {
            sb.append("DOM is acceptabel (klein bestand). ");
        }

        sb.append("JAXB voor type-safe API's.");
        this.aanbeveling = sb.toString();
    }

    // Getters
    public List<ParseResult> getResults() {
        return results;
    }

    public String getSnelste() {
        return snelste;
    }

    public String getMinsteGeheugen() {
        return minsteGeheugen;
    }

    public String getAanbeveling() {
        return aanbeveling;
    }
}