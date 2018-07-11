package edu.emory.mrp.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Reference {
    @XmlAttribute
    public String refNo;
    @XmlAttribute
    public String pmid;
    @XmlElement
    public String refName;
}
