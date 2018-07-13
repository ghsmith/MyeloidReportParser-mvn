package edu.emory.mrp.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public class MyeloidCase {
    @XmlAttribute
    public String patient;
    @XmlAttribute
    public String dob;
    @XmlAttribute
    public String gender;
    @XmlAttribute
    public String pid;
    @XmlAttribute
    public String fin;
    @XmlAttribute
    public String collectionDate;
    @XmlAttribute
    public String physician;
    @XmlAttribute
    public String specimenType;
    @XmlElement(name="diagnosis", type=String.class)
    @XmlElementWrapper(name="diagnoses")    
    public List<String> diagnoses = new ArrayList<>();
    @XmlElement(name="note", type=String.class)
    @XmlElementWrapper(name="notes")    
    public List<String> notes = new ArrayList<>();
    @XmlElement(name="variant", type=Variant.class)
    @XmlElementWrapper(name="variants")    
    public List<Variant> variants = new ArrayList<>();
    @XmlElement(name="reference", type=Reference.class)
    @XmlElementWrapper(name="references")    
    public List<Reference> references = new ArrayList<>();
    @XmlTransient
    public Map<Integer, Reference> getReferenceMapByRefNo() {
        Map<Integer, Reference> referenceMap = new HashMap<>();
        for(Reference reference : references) {
            referenceMap.put(reference.refNo, reference);
        }
        return referenceMap;
    }
}
