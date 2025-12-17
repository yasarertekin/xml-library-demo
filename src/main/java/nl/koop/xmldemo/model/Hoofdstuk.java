package nl.koop.xmldemo.model;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Hoofdstuk {
    @XmlAttribute
    private int nummer;

    @XmlElement
    private String titel;

    @XmlElement(name = "artikel")
    private List<Artikel> artikelen;

    // Getters en Setters
    public int getNummer() {
        return nummer;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public List<Artikel> getArtikelen() {
        return artikelen;
    }

    public void setArtikelen(List<Artikel> artikelen) {
        this.artikelen = artikelen;
    }
}