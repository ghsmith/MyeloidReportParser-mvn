package edu.emory.mrp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnwrappingBufferedReader extends BufferedReader {

    Pattern[] paragraphPatterns = new Pattern[] {
        Pattern.compile("^I\\. .*"),
        Pattern.compile("^II\\. .*"),
        Pattern.compile("^III\\. .*"),
        Pattern.compile("^IV\\. .*"),
        Pattern.compile("^1\\. .*"),
        Pattern.compile("^2\\. .*"),
        Pattern.compile("^3\\. .*"),
        Pattern.compile("^4\\. .*"),
        Pattern.compile("^5\\. .*"),
        Pattern.compile("^6\\. .*"),
        Pattern.compile("^7\\. .*"),
        Pattern.compile("^8\\. .*"),
        Pattern.compile("^9\\. .*"),
        Pattern.compile("^10\\. .*"),
        Pattern.compile("^11\\. .*"),
        Pattern.compile("^12\\. .*"),
        Pattern.compile("^13\\. .*"),
        Pattern.compile("^14\\. .*"),
        Pattern.compile("^15\\. .*"),
        Pattern.compile("^16\\. .*"),
        Pattern.compile("^17\\. .*"),
        Pattern.compile("^18\\. .*"),
        Pattern.compile("^19\\. .*"),
        Pattern.compile("^20\\. .*"),
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
        while((line = super.readLine()) != null) {
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
        return(line);
    }
    
}
