package edu.emory.mrp;

import edu.emory.mrp.data.Reference;
import edu.emory.mrp.jaxb.CaseVariant;
import edu.emory.mrp.jaxb.CaseVariantReference;
import edu.emory.mrp.jaxb.Dataroot;
import edu.emory.mrp.jaxb.ReferenceWithSearchPmid;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class ReferencePmidConverter {

    public static void main(String args[]) throws JAXBException {
    
        JAXBContext jc0 = JAXBContext.newInstance("edu.emory.mrp.jaxb");
        Dataroot dataroot = (Dataroot)jc0.createUnmarshaller().unmarshal(new File("data/caseVariant.xml"));

        for(CaseVariant variant : dataroot.getCaseVariant()) {
            if(variant.getInterpretation() != null) {
                variant.setInterpretation(variant.getInterpretation().replaceAll("-->\\(.*?\\)", ""));
            }
        }
        
        for(CaseVariant variant : dataroot.getCaseVariant()) {
            if(variant.getInterpretation() != null) {
                StringBuffer newInterp = new StringBuffer();
                int lastStart = 0;
                {
                    Pattern pattern = Pattern.compile(" \\(([0-9\\-, ]*)\\)"); // leading space is an attempt to avoid cytogenetic nomenclature (e.g., inv(16))
                    Matcher matcher = pattern.matcher(variant.getInterpretation());
                    while(matcher.find()) {
                        StringBuffer newRefs = new StringBuffer();
                        for(String refNoRange : matcher.group(1).split(",")) {
                            refNoRange = refNoRange.trim();
                            if(refNoRange.contains("-")) {
                                for(int refNo = new Integer(refNoRange.split("-")[0]); refNo <= new Integer(refNoRange.split("-")[1]); refNo++) {
                                    final int refNoFinal = refNo;
                                    ReferenceWithSearchPmid reference = dataroot.getReferenceWithSearchPmid().stream().filter((referenceSearch) ->
                                        referenceSearch.getReferenceId().equals(variant.getCaseVariantReference().stream().filter((caseVariantReference) ->
                                            caseVariantReference.getRefNo() == refNoFinal).findFirst().orElse(null).getReferenceId())).findFirst().orElse(null);
                                    newRefs.append((newRefs.length() > 0 ? "; " : "PMID: ") + reference.getSearchPmid().trim());
                                }
                            }
                            else {
                                final int refNoRangeFinal = Integer.parseInt(refNoRange);
                                ReferenceWithSearchPmid reference = dataroot.getReferenceWithSearchPmid().stream().filter((referenceSearch) ->
                                    referenceSearch.getReferenceId().equals(variant.getCaseVariantReference().stream().filter((caseVariantReference) ->
                                        caseVariantReference.getRefNo() == refNoRangeFinal).findFirst().orElse(null).getReferenceId())).findFirst().orElse(null);
                                newRefs.append((newRefs.length() > 0 ? "; " : "PMID: ") + reference.getSearchPmid().trim());
                            }
                        }
                        if(newRefs.length() > 0) {
                            newInterp.append(variant.getInterpretation().substring(lastStart, matcher.end()) + "-->(" + newRefs + ")");
                            lastStart = matcher.end();
                        }
                    }
                }
                newInterp.append(variant.getInterpretation().substring(lastStart));
                variant.setInterpretation(newInterp.toString());
            }
        }
        
        for(CaseVariant variant : dataroot.getCaseVariant()) {
            if(variant.getInterpretation() != null) {
                StringBuffer newInterp = new StringBuffer();
                int lastStart = 0;
                {
                    Pattern pattern = Pattern.compile(" \\((.*?)\\)"); // leading space is an attempt to avoid cytogenetic nomenclature (e.g., inv(16))
                    Matcher matcher = pattern.matcher(variant.getInterpretation());
                    while(matcher.find()) {
                        StringBuffer newRefs = new StringBuffer();
                        for(String refName : matcher.group(1).split(";")) {
                            refName = refName.trim();
                            Pattern patternInternal = Pattern.compile("(.*), ?((19|20)[0-9][0-9][a-z]?)");
                            Matcher matcherInternal = patternInternal.matcher(refName);
                            if(matcherInternal.matches()) {
                                boolean hit = false;
                                for(CaseVariantReference caseVariantReference : variant.getCaseVariantReference()) {
                                    ReferenceWithSearchPmid reference = dataroot.getReferenceWithSearchPmid().stream().filter((referenceSearch) -> referenceSearch.getReferenceId().equals(caseVariantReference.getReferenceId())).findFirst().orElse(null);
                                    if(reference.getReference().startsWith(matcherInternal.group(1).split(" ")[0]) && reference.getReference().contains(matcherInternal.group(2))) {
                                        hit = true;
                                        newRefs.append((newRefs.length() > 0 ? "; " : "PMID: ") + reference.getSearchPmid().trim());
                                        break;
                                    }
                                }
                                if(!hit) {
                                    newRefs.append((newRefs.length() > 0 ? "; " : "PMID: ") + "NONE");
                                }
                            }
                        }
                        if(newRefs.length() > 0) {
                            newInterp.append(variant.getInterpretation().substring(lastStart, matcher.end()) + "-->(" + newRefs + ")");
                            lastStart = matcher.end();
                        }
                    }
                }
                newInterp.append(variant.getInterpretation().substring(lastStart));
                variant.setInterpretation(newInterp.toString());
            }
        }
        
        System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s", "caseVariantId", "caseId", "variantId", "category", "frequency", "interpretation"));
        for(CaseVariant variant : dataroot.getCaseVariant()) {
            System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s", variant.getCaseVariantId(), variant.getCaseId(), variant.getVariantId(), variant.getCategory(), variant.getFrequency() == null ? "" : variant.getFrequency(), variant.getInterpretation() == null ? "" : variant.getInterpretation()));
        }
        
    }
    
}
