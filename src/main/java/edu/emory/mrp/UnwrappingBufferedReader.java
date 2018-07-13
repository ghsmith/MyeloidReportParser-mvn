package edu.emory.mrp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnwrappingBufferedReader extends BufferedReader {

    Pattern[] paragraphPatterns = new Pattern[] {
        Pattern.compile("Note: .*"),
        Pattern.compile("^I\\. .*"),
        Pattern.compile("^II\\. .*"),
        Pattern.compile("^III\\. .*"),
        Pattern.compile("^IV\\. .*"),
        Pattern.compile("^1\\..*"), // some of these cases have huge numbers of references
        Pattern.compile("^2\\..*"),
        Pattern.compile("^3\\..*"),
        Pattern.compile("^4\\..*"),
        Pattern.compile("^5\\..*"),
        Pattern.compile("^6\\..*"),
        Pattern.compile("^7\\..*"),
        Pattern.compile("^8\\..*"),
        Pattern.compile("^9\\..*"),
        Pattern.compile("^10\\..*"),
        Pattern.compile("^11\\..*"),
        Pattern.compile("^12\\..*"),
        Pattern.compile("^13\\..*"),
        Pattern.compile("^14\\..*"),
        Pattern.compile("^15\\..*"),
        Pattern.compile("^16\\..*"),
        Pattern.compile("^17\\..*"),
        Pattern.compile("^18\\..*"),
        Pattern.compile("^19\\..*"),
        Pattern.compile("^20\\..*"),
        Pattern.compile("^21\\..*"),
        Pattern.compile("^22\\..*"),
        Pattern.compile("^23\\..*"),
        Pattern.compile("^24\\..*"),
        Pattern.compile("^25\\..*"),
        Pattern.compile("^26\\..*"),
        Pattern.compile("^27\\..*"),
        Pattern.compile("^28\\..*"),
        Pattern.compile("^29\\..*"),
        Pattern.compile("^30\\..*"),
        Pattern.compile("^31\\..*"),
        Pattern.compile("^32\\..*"),
        Pattern.compile("^33\\..*"),
        Pattern.compile("^34\\..*"),
        Pattern.compile("^35\\..*"),
        Pattern.compile("^36\\..*"),
        Pattern.compile("^37\\..*"),
        Pattern.compile("^38\\..*"),
        Pattern.compile("^39\\..*"),
        Pattern.compile("^40\\..*"),
        Pattern.compile("^41\\..*"),
        Pattern.compile("^42\\..*"),
        Pattern.compile("^43\\..*"),
        Pattern.compile("^44\\..*"),
        Pattern.compile("^45\\..*"),
        Pattern.compile("^46\\..*"),
        Pattern.compile("^47\\..*"),
        Pattern.compile("^48\\..*"),
        Pattern.compile("^49\\..*"),
        Pattern.compile("^50\\..*"),
        Pattern.compile("^51\\..*"),
        Pattern.compile("^52\\..*"),
        Pattern.compile("^53\\..*"),
        Pattern.compile("^54\\..*"),
        Pattern.compile("^55\\..*"),
        Pattern.compile("^56\\..*"),
        Pattern.compile("^57\\..*"),
        Pattern.compile("^58\\..*"),
        Pattern.compile("^59\\..*"),
        Pattern.compile("^60\\..*"),
        Pattern.compile("^61\\..*"),
        Pattern.compile("^62\\..*"),
        Pattern.compile("^63\\..*"),
        Pattern.compile("^64\\..*"),
        Pattern.compile("^65\\..*"),
        Pattern.compile("^66\\..*"),
        Pattern.compile("^67\\..*"),
        Pattern.compile("^68\\..*"),
        Pattern.compile("^69\\..*"),
        Pattern.compile("^70\\..*"),
        Pattern.compile("^71\\..*"),
        Pattern.compile("^72\\..*"),
        Pattern.compile("^73\\..*"),
        Pattern.compile("^74\\..*"),
        Pattern.compile("^75\\..*"),
        Pattern.compile("^76\\..*"),
        Pattern.compile("^77\\..*"),
        Pattern.compile("^78\\..*"),
        Pattern.compile("^79\\..*"),
        Pattern.compile("^80\\..*"),
        Pattern.compile("^81\\..*"),
        Pattern.compile("^82\\..*"),
        Pattern.compile("^83\\..*"),
        Pattern.compile("^84\\..*"),
        Pattern.compile("^85\\..*"),
        Pattern.compile("^86\\..*"),
        Pattern.compile("^87\\..*"),
        Pattern.compile("^88\\..*"),
        Pattern.compile("^89\\..*"),
        Pattern.compile("^90\\..*"),
        Pattern.compile("^91\\..*"),
        Pattern.compile("^92\\..*"),
        Pattern.compile("^93\\..*"),
        Pattern.compile("^94\\..*"),
        Pattern.compile("^95\\..*"),
        Pattern.compile("^96\\..*"),
        Pattern.compile("^97\\..*"),
        Pattern.compile("^98\\..*"),
        Pattern.compile("^99\\..*"),
        Pattern.compile("^Interpretation: .*")
    };

    public UnwrappingBufferedReader(Reader in) {
        super(in);
    }

    @Override
    public String readLine() throws IOException {
        boolean inParagraph = false;
        StringBuffer unwrappedLine = new StringBuffer();
        String line;
        super.mark(10000);
        while((line = super.readLine()) != null) {
            line = line.trim();
            if(inParagraph) {
                if("".equals(line)) {
                    super.reset();
                    return(unwrappedLine.toString());
                }
                else {
                    for(Pattern pattern : paragraphPatterns) {
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.matches()) {
                            super.reset();
                            return(unwrappedLine.toString());
                        }
                    }
                }
            }
            else {
                for(Pattern pattern : paragraphPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        inParagraph = true;
                        break;
                    }
                }
            }
            if(inParagraph) {
                unwrappedLine.append((unwrappedLine.length() == 0 ? "" : " ") + line.trim());
                super.mark(10000);
            }
            else {
                return(line.trim());
            }
        }
        return(null);
    }
    
}
