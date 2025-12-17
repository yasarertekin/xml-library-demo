package nl.koop.xmldemo.model;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Artikel {
    @XmlAttribute
    private String id;

    @XmlElement
    private String kop;

    @XmlElement(name = "lid")
    private List<Lid> leden;

    // Getters en Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKop() {
        return kop;
    }

    public void setKop(String kop) {
        this.kop = kop;
    }

    public List<Lid> getLeden() {
        return leden;
    }

    public void setLeden(List<Lid> leden) {
        this.leden = leden;
    }
}