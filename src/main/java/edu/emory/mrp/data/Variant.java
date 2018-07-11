package edu.emory.mrp.data;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

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
    @XmlTransient
    public SortedSet<Integer> refNos = new TreeSet<>();
    @XmlAttribute
    public String getRefNoList() {
        return refNos.toString();
    }
}
