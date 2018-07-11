package edu.emory.mrp.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MyeloidCases {
    public List<MyeloidCase> myeloidCase = new ArrayList<>();
}
