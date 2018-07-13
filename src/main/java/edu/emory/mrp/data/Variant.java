package edu.emory.mrp.data;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public class Variant {
    @XmlTransient
    public MyeloidCase myeloidCase;
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
    // for flat...
    @XmlTransient
    // ...for flat
    // for tall...
    //@XmlElement(name="refNo", type=Integer.class)
    //@XmlElementWrapper(name="refNos")    
    // ...for tall
    public SortedSet<Integer> refNos = new TreeSet<>();
    // for flat...
    @XmlAttribute
    public String getRefNoList() {
        return refNos.toString();
    }
    // ...for flat
    // for tall...
    //@XmlElement(name="refHash", type=Integer.class)
    //@XmlElementWrapper(name="refHashes")    
    //public SortedSet<Integer> getRefHashes() {
    //    SortedSet<Integer> refHashes = new TreeSet<>();
    //    for(Integer refNo : refNos) {
    //        refHashes.add(myeloidCase.getReferenceMapByRefNo().get(refNo).hashCode());
    //    }
    //    return refHashes;
    //}
    // ...for tall
    // for flat...
    @XmlAttribute
    public String getRefHashList() {
        SortedSet<Integer> refHashes = new TreeSet<>();
        for(Integer refNo : refNos) {
            refHashes.add(myeloidCase.getReferenceMapByRefNo().get(refNo).hashCode());
        }
        return refHashes.toString();
    }
    // ...for flat
}
