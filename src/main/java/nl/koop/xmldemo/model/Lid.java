package nl.koop.xmldemo.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Lid {
    @XmlAttribute
    private int nummer;
    
    @XmlValue
    private String text;
    
    // Getters en Setters
    public int getNummer() { 
        return nummer; 
    }
    
    public void setNummer(int nummer) { 
        this.nummer = nummer; 
    }
    
    public String getText() { 
        return text; 
    }
    
    public void setText(String text) { 
        this.text = text; 
    }
}