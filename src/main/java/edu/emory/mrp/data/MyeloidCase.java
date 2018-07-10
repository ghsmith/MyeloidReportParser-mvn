package edu.emory.mrp.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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
    @XmlElement(name="variant", type=Variant.class)
    @XmlElementWrapper(name="variants")    
    public List<Variant> variants = new ArrayList<>();
}
