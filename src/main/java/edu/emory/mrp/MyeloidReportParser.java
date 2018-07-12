package edu.emory.mrp;

import edu.emory.mrp.data.MyeloidCase;
import edu.emory.mrp.data.MyeloidCases;
import edu.emory.mrp.data.Reference;
import edu.emory.mrp.data.References;
import edu.emory.mrp.data.Variant;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author ghsmith
 */
public class MyeloidReportParser {

    public static void main(String[] args) throws IOException, JAXBException {

        MyeloidCases myeloidCases = new MyeloidCases();
        References references = new References();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fileName;
        while((fileName = stdIn.readLine()) != null) {

            MyeloidCase myeloidCase = new MyeloidCase();

            {
                String section = "none";
                myeloidCases.myeloidCase.add(myeloidCase);
                String variantCategory = "none";
                Variant variant = null;

                UnwrappingBufferedReader fileIn = new UnwrappingBufferedReader(new FileReader(fileName));
                boolean inPreBlock = false;
                String line;
                while((line = fileIn.readLine()) != null) {
                    if(line.startsWith("<pre")) {
                        inPreBlock = true;
                    }
                    else if(line.startsWith("</pre")) {
                        inPreBlock = false;
                    }
                    if(inPreBlock) {
                        System.err.println(String.format("%s: %s", fileName, line));
                        if     (line.startsWith("Patient Report")) { section = "demographics"; }
                        else if(line.startsWith("Result:"))        { section = "results"; }
                        else if(line.startsWith("References:"))    { section = "references"; }
                        if(section.equals("demographics")) {
                            {
                                Pattern pattern = Pattern.compile("^Patient: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.patient = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^DOB: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.dob = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^GENDER: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.gender = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Patient Identifiers: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.pid = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Visit Number (FIN): (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.fin = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Collection Date: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.collectionDate = matcher.group(1).trim();
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Physician: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.physician = matcher.group(1).trim();
                                }
                            }
                        }
                        if(section.equals("results")) {
                            if     (line.startsWith("I. Tier 1"))                            { variantCategory = "Tier 1: Known significance"; }
                            else if(line.startsWith("II. Tier 2"))                           { variantCategory = "Tier 2: Unknown significance"; }
                            else if(line.startsWith("III. Single Nucleotide Polymorphisms")) { variantCategory = "SNP with significance"; }
                            {
                                Pattern pattern = Pattern.compile("^[0-9]+\\. ([^ ]+) (c\\.[^ ]+), (p\\.[^ ]+) \\((NM_[0-9\\.]+)\\) Variant Frequency: <?([0-9\\.]+)%");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    variant = new Variant();
                                    myeloidCase.variants.add(variant);
                                    variant.myeloidCase = myeloidCase;
                                    variant.category = variantCategory;
                                    variant.gene = matcher.group(1);
                                    variant.hgvsc = matcher.group(2);
                                    variant.hgvsp = matcher.group(3);
                                    variant.transcript = matcher.group(4);
                                    variant.frequency = String.format("%4.1f", new Float(matcher.group(5)));
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^[0-9]+\\. ([^ ]+) (c\\.[^ ]+) \\((NM_[0-9\\.]+)\\) Variant Frequency: <?([0-9\\.]+)%");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    variant = new Variant();
                                    variant.myeloidCase = myeloidCase;
                                    myeloidCase.variants.add(variant);
                                    variant.category = variantCategory;
                                    variant.gene = matcher.group(1);
                                    variant.hgvsc = matcher.group(2);
                                    variant.transcript = matcher.group(3);
                                    variant.frequency = String.format("%4.1f", new Float(matcher.group(4)));
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Interpretation: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    variant.interpretation = matcher.group(1);
                                }
                            }
                        }
                        if(section.equals("references")) {
                            {
                                Pattern pattern1 = Pattern.compile("(^[0-9]+)\\.(.*) PubMed PMID: ([0-9]+)\\.");
                                Pattern pattern2 = Pattern.compile("(^[0-9]+)\\.(.*) PMID: ([0-9]+)\\.");
                                Pattern pattern3 = Pattern.compile("(^[0-9]+)\\.(.*)");
                                Matcher matcher1 = pattern1.matcher(line);
                                if(matcher1.matches()) {
                                    Reference reference = new Reference();
                                    myeloidCase.references.add(reference);
                                    reference.refNo = new Integer(matcher1.group(1));
                                    reference.pmid = matcher1.group(3);
                                    reference.refName = matcher1.group(2).trim();
                                    Reference globalReference = references.getReferenceMapByRefHash().get(reference.getRefHash());
                                    if(globalReference == null) {
                                        globalReference = new Reference();
                                        globalReference.pmid = matcher1.group(3);
                                        globalReference.refName = matcher1.group(2).trim();
                                        references.reference.add(globalReference);
                                    }
                                }
                                else {
                                    Matcher matcher2 = pattern2.matcher(line);
                                    if(matcher2.matches()) {
                                        Reference reference = new Reference();
                                        myeloidCase.references.add(reference);
                                        reference.refNo = new Integer(matcher2.group(1));
                                        reference.pmid = matcher2.group(3);
                                        reference.refName = matcher2.group(2).trim();
                                        Reference globalReference = references.getReferenceMapByRefHash().get(reference.getRefHash());
                                        if(globalReference == null) {
                                            globalReference = new Reference();
                                            globalReference.pmid = matcher2.group(3);
                                            globalReference.refName = matcher2.group(2).trim();
                                            references.reference.add(globalReference);
                                        }
                                    }
                                    else {
                                        Matcher matcher3 = pattern3.matcher(line);
                                        if(matcher3.matches()) {
                                            Reference reference = new Reference();
                                            myeloidCase.references.add(reference);
                                            reference.refNo = new Integer(matcher3.group(1));
                                            reference.refName = matcher3.group(2).trim();
                                            Reference globalReference = references.getReferenceMapByRefHash().get(reference.getRefHash());
                                            if(globalReference == null) {
                                                globalReference = new Reference();
                                                globalReference.refName = matcher3.group(2).trim();
                                                references.reference.add(globalReference);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                fileIn.close();
            }

            // attempt to parse references
            {
                for(Variant variant : myeloidCase.variants) {
                    {
                        Pattern pattern = Pattern.compile(" \\(([0-9\\-, ]*)\\)"); // leading space is an attempt to avoid cytogenetic nomenclature (e.g., inv(16))
                        Matcher matcher = pattern.matcher(variant.interpretation);
                        while(matcher.find()) {
                            for(String refNoRange : matcher.group(1).split(",")) {
                                refNoRange = refNoRange.trim();
                                if(refNoRange.contains("-")) {
                                    for(int refNo = new Integer(refNoRange.split("-")[0]); refNo <= new Integer(refNoRange.split("-")[1]); refNo++) {
                                        variant.refNos.add(refNo);
                                        myeloidCase.getReferenceMapByRefNo().get(refNo).geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                        references.getReferenceMapByRefHash().get(myeloidCase.getReferenceMapByRefNo().get(refNo).getRefHash()).geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                    }
                                }
                                else {
                                    variant.refNos.add(new Integer(refNoRange));
                                    myeloidCase.getReferenceMapByRefNo().get(new Integer(refNoRange)).geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                    references.getReferenceMapByRefHash().get(myeloidCase.getReferenceMapByRefNo().get(new Integer(refNoRange)).getRefHash()).geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                }
                            }
                        }
                    }
                    {
                        Pattern pattern = Pattern.compile(" \\((.*?)\\)"); // leading space is an attempt to avoid cytogenetic nomenclature (e.g., inv(16))
                        Matcher matcher = pattern.matcher(variant.interpretation);
                        while(matcher.find()) {
                            for(String refName : matcher.group(1).split(";")) {
                                refName = refName.trim();
                                Pattern patternInternal = Pattern.compile("(.*), ?((19|20)[0-9][0-9][a-z]?)");
                                Matcher matcherInternal = patternInternal.matcher(refName);
                                if(matcherInternal.matches()) {
                                    for(Reference reference : myeloidCase.references) {
                                        if(reference.refName.startsWith(matcherInternal.group(1).split(" ")[0]) && reference.refName.contains(matcherInternal.group(2))) {
                                            variant.refNos.add(reference.refNo);
                                            reference.geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                            references.getReferenceMapByRefHash().get(reference.getRefHash()).geneHgvscs.add(variant.gene + " " + variant.hgvsc);
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        
        }

        if(args[0].equals("myeloidCases")) {
            JAXBContext jc = JAXBContext.newInstance(new Class[] {MyeloidCases.class});
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            m.marshal(myeloidCases, System.out);
        }

        if(args[0].equals("references")) {
            JAXBContext jc = JAXBContext.newInstance(new Class[] {References.class});
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            m.marshal(references, System.out);
        }
        
    }
    
}
