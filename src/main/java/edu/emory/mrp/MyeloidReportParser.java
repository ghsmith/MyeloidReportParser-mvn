package edu.emory.mrp;

import edu.emory.mrp.data.MyeloidCase;
import edu.emory.mrp.data.MyeloidCases;
import edu.emory.mrp.data.Reference;
import edu.emory.mrp.data.References;
import edu.emory.mrp.data.Variant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
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

    public static void main(String[] args) throws IOException, JAXBException, ParseException {

        int fileNo = 0;
        
        MyeloidCases myeloidCases = new MyeloidCases();
        References references = new References();

        // Get some cardinality for Excel to chew on...
        /*{
            MyeloidCase myeloidCase = new MyeloidCase();
            myeloidCases.myeloidCase.add(myeloidCase);
            myeloidCase.patient = "Template";
            myeloidCase.diagnoses.add("Diagnosis #1");
            myeloidCase.diagnoses.add("Diagnosis #2");
            myeloidCase.notes.add("Note #1");
            myeloidCase.notes.add("Note #2");
        }*/
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fileName;
        while((fileName = stdIn.readLine()) != null) {

            fileNo++;
            
            if(args.length > 0 && args[0] != null && fileNo < new Integer(args[0])) {
                continue;
            }
            
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
                        System.err.println(String.format("[%3d] %s: %s", fileNo, fileName, line));
                        if     (line.startsWith("Patient Report")) { section = "demographics"; }
                        else if(line.startsWith("Result:"))        { section = "results"; }
                        else if(line.startsWith("References:"))    { section = "references"; }
                        if(section.equals("demographics")) {
                            {
                                Pattern pattern = Pattern.compile("^Patient: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.patient = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^DOB: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.dob = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^GENDER: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.gender = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Patient Identifiers: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.pid = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Visit Number \\(FIN\\): (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.fin = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Collection Date: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.collectionDate = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^PHYSICIAN: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.physician = matcher.group(1).trim();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    fileIn.readLine();
                                    fileIn.readLine();
                                    line = fileIn.readLine();
                                    myeloidCase.specimenType = line;
                                    fileIn.readLine();
                                    fileIn.readLine();
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Submitted diagnosis: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.diagnoses.add(matcher.group(1).trim());
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^interpretation:");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    line = fileIn.readLine();
                                    myeloidCase.diagnoses.add(line);
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Note: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    myeloidCase.notes.add(matcher.group(1).trim());
                                    continue;
                                }
                            }
                        }
                        if(section.equals("results")) {
                            if     (line.startsWith("I. Tier 1"))                            { variantCategory = "Tier 1: Known significance"; }
                            else if(line.startsWith("II. Tier 2"))                           { variantCategory = "Tier 2: Unknown significance"; }
                            else if(line.startsWith("III. Single Nucleotide Polymorphisms")) { variantCategory = "SNP with significance"; }
                            {
                                Pattern pattern = Pattern.compile("^[0-9]+\\. +([^ ]+) +(c\\.[^ ]+), +(p\\.[^ ]+) +\\((NM_[0-9\\.]+)\\) +Variant Frequency: +<?([0-9\\.]+)%.*");
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
                                    continue;
                                }
                            }
                            {   // no comma...
                                Pattern pattern = Pattern.compile("^[0-9]+\\. +([^ ]+) +(c\\.[^ ]+) +(p\\.[^ ]+) +\\((NM_[0-9\\.]+)\\) +Variant Frequency: +<?([0-9\\.]+)%.*");
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
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^[0-9]+\\. +([^ ]+) +(c\\.[^ ]+), +(p\\.[^ ]+) +\\((NM_[0-9\\.]+)\\) +Variant Frequency: Not reported.*");
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
                                    variant.frequency = String.format("%4.1f", new Float(999));
                                    continue;
                                }
                            }
                            {   // no comma...
                                Pattern pattern = Pattern.compile("^[0-9]+\\. +([^ ]+) +(c\\.[^ ]+) +(p\\.[^ ]+) +\\((NM_[0-9\\.]+)\\) +Variant Frequency: Not reported.*");
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
                                    variant.frequency = String.format("%4.1f", new Float(999));
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("^[0-9]+\\. +([^ ]+) +(c\\.[^ ]+) +\\((NM_[0-9\\.]+)\\) +Variant Frequency: +<?([0-9\\.]+)%.*");
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
                                    continue;
                                }
                            }
                            {
                                Pattern pattern = Pattern.compile("(?i)^NONE DETECTED");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    variant = new Variant();
                                    myeloidCase.variants.add(variant);
                                    variant.myeloidCase = myeloidCase;
                                    variant.category = variantCategory;
                                    variant.gene = "NONE DETECTED";
                                    continue;
                               }
                            }
                            {
                                Pattern pattern = Pattern.compile("^Interpretation: (.*)");
                                Matcher matcher = pattern.matcher(line);
                                if(matcher.matches()) {
                                    variant.interpretation = matcher.group(1);
                                    continue;
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
                    if(variant.interpretation != null) {
                        {
                            Pattern pattern = Pattern.compile(" \\(([0-9\\-, ]*)\\)"); // leading space is an attempt to avoid cytogenetic nomenclature (e.g., inv(16))
                            Matcher matcher = pattern.matcher(variant.interpretation);
                            while(matcher.find()) {
                                for(String refNoRange : matcher.group(1).split(",")) {
                                    refNoRange = refNoRange.trim();
                                    if(refNoRange.contains("-")) {
                                        for(int refNo = new Integer(refNoRange.split("-")[0]); refNo <= new Integer(refNoRange.split("-")[1]); refNo++) {
                                            variant.refNos.add(refNo);
                                            myeloidCase.getReferenceMapByRefNo().get(refNo).geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);
                                            references.getReferenceMapByRefHash().get(myeloidCase.getReferenceMapByRefNo().get(refNo).getRefHash()).geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);
                                        }
                                    }
                                    else {
                                        variant.refNos.add(new Integer(refNoRange));
                                        myeloidCase.getReferenceMapByRefNo().get(new Integer(refNoRange)).geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);
                                        references.getReferenceMapByRefHash().get(myeloidCase.getReferenceMapByRefNo().get(new Integer(refNoRange)).getRefHash()).geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);
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
                                                reference.geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);
                                                references.getReferenceMapByRefHash().get(reference.getRefHash()).geneHgvscs.add(variant.gene + " " + variant.transcript + ":" + variant.hgvsc);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        
        }

        /*{
            JAXBContext jc = JAXBContext.newInstance(new Class[] {MyeloidCases.class});
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            m.marshal(myeloidCases, new FileOutputStream(new File("myeloid_cases.xml")));
        }

        {
            JAXBContext jc = JAXBContext.newInstance(new Class[] {References.class});
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            m.marshal(references, new FileOutputStream(new File("references.xml")));
        }*/

        // flat files for Access
        {
        
            class FlatMyeloidCase {
                public int caseId;
                public String patient;
                public String dob;
                public String gender;
                public String pid;
                public String fin;
                public String collectionDate;
                public String physician;
                public String specimenType;
                public String diagnosis;
                public String note;
            }
            
            class FlatVariant {
                public int variantId;
                public String gene;
                public String transcript;
                public String hgvsc;
                public String hgvsp;
            }

            class FlatCaseVariant {
                public int caseVariantId;
                public int caseId;
                public int variantId;
                public String category;
                public String frequency;
                public String interpretation;
            }
            
            class FlatReference {
                public int referenceId;
                public String reference;
                public String pmid;
            }

            class FlatCaseVariantReference {
                public int caseVariantReferenceId;
                public int caseVariantId;
                public int referenceId;
                public int caseId;
                public int variantId;
                public String refNo;
            }

            
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy kk:mm");

            Map<Integer, FlatMyeloidCase> flatMyeloidCaseMap = new HashMap<>();
            Map<Integer, FlatVariant> flatVariantMap = new HashMap<>();
            Map<Integer, FlatCaseVariant> flatCaseVariantMap = new HashMap<>();
            Map<Integer, FlatReference> flatReferenceMap = new HashMap<>();
            Map<Integer, FlatCaseVariantReference> flatCaseVariantReferenceMap = new HashMap<>();
            
            for(MyeloidCase myeloidCase : myeloidCases.myeloidCase) {
                
                FlatMyeloidCase fmc = new FlatMyeloidCase();
                fmc.caseId = Math.abs((myeloidCase.patient + myeloidCase.dob + myeloidCase.pid + myeloidCase.fin).hashCode());
                if(flatMyeloidCaseMap.get(fmc.caseId) != null) {
                    throw new RuntimeException("non-unique hash");
                }
                fmc.patient = myeloidCase.patient;
                fmc.dob = myeloidCase.dob;
                fmc.gender = myeloidCase.gender;
                fmc.pid = myeloidCase.pid;
                fmc.fin = myeloidCase.fin;
                fmc.collectionDate = myeloidCase.collectionDate;
                fmc.physician = myeloidCase.physician;
                fmc.specimenType = myeloidCase.specimenType;
                fmc.diagnosis = myeloidCase.diagnoses.isEmpty() ? "" : myeloidCase.diagnoses.get(0);
                fmc.note = myeloidCase.notes.isEmpty() ? "" : myeloidCase.notes.get(0);
                flatMyeloidCaseMap.put(fmc.caseId, fmc);
                
                for(Variant variant : myeloidCase.variants) {
                    
                    int variantId = Math.abs((variant.gene + variant.transcript + variant.hgvsc).hashCode());

                    FlatVariant fv = flatVariantMap.get(variantId);
                    if(fv == null) {
                        fv = new FlatVariant();
                        fv.variantId = variantId;
                        fv.gene = variant.gene;
                        fv.transcript = variant.transcript;
                        fv.hgvsc = variant.hgvsc;
                        fv.hgvsp = variant.hgvsp;
                        flatVariantMap.put(fv.variantId, fv);
                    }
                    if(!(fv.gene + fv.transcript + fv.hgvsc).equals(variant.gene + variant.transcript + variant.hgvsc)) {
                        throw new RuntimeException("non-unique hash");
                    }

                    FlatCaseVariant fcv = new FlatCaseVariant();
                    fcv.caseVariantId = Math.abs(("" + fmc.caseId + fv.variantId + variant.category).hashCode());
                    if(flatCaseVariantMap.get(fcv.caseVariantId) != null) {
                        throw new RuntimeException("non-unique hash");
                    }
                    fcv.caseId = fmc.caseId;
                    fcv.variantId = fv.variantId;
                    fcv.category = variant.category;
                    fcv.frequency = variant.frequency;
                    fcv.interpretation = variant.interpretation;
                    flatCaseVariantMap.put(fcv.caseVariantId, fcv);

                    for(Integer refNo : variant.refNos) {
                        
                        Reference reference = myeloidCase.getReferenceMapByRefNo().get(refNo);
                        
                        int referenceId = Math.abs(reference.refName.hashCode());
                        
                        FlatReference fr = flatReferenceMap.get(referenceId);
                        if(fr == null) {
                            fr = new FlatReference();
                            fr.referenceId = referenceId;
                            fr.reference = reference.refName;
                            fr.pmid = reference.pmid;
                            flatReferenceMap.put(fr.referenceId, fr);
                        }
                        if(!fr.reference.equals(reference.refName)) {
                            throw new RuntimeException("non-unique hash");
                        }

                        FlatCaseVariantReference fcvr = new FlatCaseVariantReference();
                        fcvr.caseVariantReferenceId = Math.abs(("" + fcv.caseVariantId + fr.referenceId + reference.refNo).hashCode());
                        if(flatCaseVariantReferenceMap.get(fcvr.caseVariantReferenceId) != null) {
                            throw new RuntimeException("non-unique hash");
                        }
                        fcvr.caseVariantId = fcv.caseVariantId;
                        fcvr.referenceId = fr.referenceId;
                        fcvr.caseId = fmc.caseId;
                        fcvr.variantId = fv.variantId;
                        fcvr.refNo = refNo.toString();
                        flatCaseVariantReferenceMap.put(fcvr.caseVariantReferenceId, fcvr);

                    }
                    
                }
                
            }
            
            {
                PrintStream f = new PrintStream(new FileOutputStream(new File("myeloid_case.txt")));
                f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                    "caseId",
                    "patient",
                    "dob",
                    "gender",
                    "pid",
                    "fin",
                    "collectionDate",
                    "physician",
                    "specimenType",
                    "diagnosis",
                    "note"
                ));
                for(FlatMyeloidCase fmc : flatMyeloidCaseMap.values()) {
                    String parsedCollectionDate = null;
                    try {
                        parsedCollectionDate = fmc.collectionDate == null ? "" : sdf1.format(sdf2.parse(fmc.collectionDate));
                    }
                    catch(ParseException e) {
                        parsedCollectionDate = fmc.collectionDate == null ? "" : sdf1.format(sdf1.parse(fmc.collectionDate));
                    }
                    f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                        fmc.caseId,
                        fmc.patient == null ? "" : fmc.patient,
                        fmc.dob == null ? "" : sdf1.format(sdf1.parse(fmc.dob)),
                        fmc.gender == null ? "" : fmc.gender,
                        fmc.pid == null ? "" : fmc.pid,
                        fmc.fin == null ? "" : fmc.fin,
                        parsedCollectionDate,
                        fmc.physician == null ? "" : fmc.physician,
                        fmc.specimenType == null ? "" : fmc.specimenType,
                        fmc.diagnosis == null ? "" : fmc.diagnosis,
                        fmc.note == null ? "" : fmc.note
                    ));
                }
            }

            {
                PrintStream f = new PrintStream(new FileOutputStream(new File("variant.txt")));
                f.println(String.format("%s\t%s\t%s\t%s\t%s",
                    "variantId",
                    "gene",
                    "transcript",
                    "hgvsc",
                    "hgvsp"
                ));
                for(FlatVariant fv : flatVariantMap.values()) {
                    f.println(String.format("%s\t%s\t%s\t%s\t%s",
                        fv.variantId,
                        fv.gene == null ? "" : fv.gene,
                        fv.transcript == null ? "" : fv.transcript,
                        fv.hgvsc == null ? "" : fv.hgvsc,
                        fv.hgvsp == null ? "" : fv.hgvsp
                    ));
                }
            }
            
            {
                PrintStream f = new PrintStream(new FileOutputStream(new File("case_variant.txt")));
                f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                    "caseVariantId",
                    "caseId",
                    "variantId",
                    "category",
                    "frequency",
                    "interpretation"
                ));
                for(FlatCaseVariant fcv : flatCaseVariantMap.values()) {
                    f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                        fcv.caseVariantId,
                        fcv.caseId,
                        fcv.variantId,
                        fcv.category == null ? "" : fcv.category,
                        fcv.frequency == null ? "" : fcv.frequency,
                        fcv.interpretation == null ? "" : fcv.interpretation
                    ));
                }
            }
            
            {
                PrintStream f = new PrintStream(new FileOutputStream(new File("reference.txt")));
                f.println(String.format("%s\t%s\t%s",
                    "referenceId",
                    "reference",
                    "pmid"
                ));
                for(FlatReference fr : flatReferenceMap.values()) {
                    f.println(String.format("%s\t%s\t%s",
                        fr.referenceId,
                        fr.reference == null ? "" : fr.reference,
                        fr.pmid == null ? "" : fr.pmid
                    ));
                }
            }

            {
                PrintStream f = new PrintStream(new FileOutputStream(new File("case_variant_reference.txt")));
                f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                    "caseVariantReferenceId",
                    "caseVariantId",
                    "referenceId",
                    "caseId",
                    "variantId",
                    "refNo"
                ));
                for(FlatCaseVariantReference fcvr : flatCaseVariantReferenceMap.values()) {
                    f.println(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                        fcvr.caseVariantReferenceId,
                        fcvr.caseVariantId,
                        fcvr.referenceId,
                        fcvr.caseId,
                        fcvr.variantId,
                        fcvr.refNo == null ? "" : fcvr.refNo
                    ));
                }
            }
            
        }
        
    }
    
}
