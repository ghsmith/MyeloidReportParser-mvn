package edu.emory.mrp.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class References {
    @XmlElement
    public SortedSet<Reference> reference = new TreeSet<>(new Comparator<Reference>() {
        @Override
        public int compare(Reference t, Reference t1) {
            return((new Integer(t.getRefHash())).compareTo(new Integer(t1.getRefHash())));
        }
    });
    @XmlTransient
    public Map<Integer, Reference> getReferenceMapByRefHash() {
        Map<Integer, Reference> referenceMap = new HashMap<>();
        for(Reference reference : reference) {
            referenceMap.put(reference.getRefHash(), reference);
        }
        return referenceMap;
    }
}
