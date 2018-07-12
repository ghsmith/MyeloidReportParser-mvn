package edu.emory.mrp.data;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public class Reference {
    @XmlAttribute
    public Integer refNo;
    @XmlAttribute
    public String pmid;
    @XmlElement
    public String refName;
    //@XmlTransient
    @XmlElement(name="geneHgvsc", type=String.class)
    @XmlElementWrapper(name="geneHgvscs")    
    public SortedSet<String> geneHgvscs = new TreeSet<>();
    //@XmlAttribute
    //public String getGeneHgvscList() {
    //    return geneHgvscs.toString();
    //}
    @XmlAttribute
    public int getRefHash() {
        return Math.abs(refName.hashCode());
    }
}
