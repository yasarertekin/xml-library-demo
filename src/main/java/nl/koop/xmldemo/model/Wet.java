package nl.koop.xmldemo.model;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "wet")
@XmlAccessorType(XmlAccessType.FIELD)
public class Wet {
    @XmlAttribute
    private String id;
    
    @XmlElement
    private Metadata metadata;
    
    @XmlElement(name = "hoofdstuk")
    private List<Hoofdstuk> hoofdstukken;
    
    // Getters en Setters
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public Metadata getMetadata() { 
        return metadata; 
    }
    
    public void setMetadata(Metadata metadata) { 
        this.metadata = metadata; 
    }
    
    public List<Hoofdstuk> getHoofdstukken() { 
        return hoofdstukken; 
    }
    
    public void setHoofdstukken(List<Hoofdstuk> hoofdstukken) { 
        this.hoofdstukken = hoofdstukken; 
    }
}