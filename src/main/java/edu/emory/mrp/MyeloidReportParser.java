package edu.emory.mrp;

import edu.emory.mrp.data.MyeloidCase;
import edu.emory.mrp.data.Variant;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

        MyeloidCase myeloidCase = null;
        Variant variant = null;
        
        UnwrappingBufferedReader stdIn = new UnwrappingBufferedReader(new InputStreamReader(System.in));
        boolean inPreBlock = false;
        String line;
        while((line = stdIn.readLine()) != null) {
            if(line.startsWith("<pre")) {
                inPreBlock = true;
            }
            else if(line.startsWith("</pre")) {
                inPreBlock = false;
            }
            if(inPreBlock) {
                {
                    Pattern pattern = Pattern.compile("^Patient: (.*)");
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        myeloidCase = new MyeloidCase();
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
                {
                    Pattern pattern = Pattern.compile("^[0-9]+\\. ([^ ]+) (c\\.[^ ]+), (p\\.[^ ]+) \\((NM_[0-9\\.]+)\\) Variant Frequency: ([0-9\\.]+)%");
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        variant = new Variant();
                        myeloidCase.variants.add(variant);
                        variant.category = "?";
                        variant.gene = matcher.group(1);
                        variant.hgvsc = matcher.group(2);
                        variant.hgvsp = matcher.group(3);
                        variant.transcript = matcher.group(4);
                        variant.frequency = matcher.group(5);
                    }
                }
                {
                    Pattern pattern = Pattern.compile("^[0-9]+\\. ([^ ]+) (c\\.[^ ]+) \\((NM_[0-9\\.]+)\\) Variant Frequency: ([0-9\\.]+)%");
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        variant = new Variant();
                        myeloidCase.variants.add(variant);
                        variant.category = "?";
                        variant.gene = matcher.group(1);
                        variant.hgvsc = matcher.group(2);
                        variant.transcript = matcher.group(3);
                        variant.frequency = matcher.group(4);
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
        }

        JAXBContext jc = JAXBContext.newInstance(new Class[] {MyeloidCase.class});
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        m.marshal(myeloidCase, System.out);        
        
    }
    
}
