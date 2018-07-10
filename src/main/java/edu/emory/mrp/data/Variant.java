package edu.emory.mrp.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Variant {
    @XmlAttribute
    public String category;
    @XmlAttribute
    public String gene;
    @XmlAttribute
    public String hgvsc;
    @XmlAttribute
    public String hgvsp;
    @XmlAttribute
    public String transcript;
    @XmlAttribute
    public String frequency;
    @XmlElement
    public String interpretation;
}
