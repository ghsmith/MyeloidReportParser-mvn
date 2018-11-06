package edu.emory.mrp;

import generated.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author pyewacket
 */
public class CitationFinder {

    public static void main(String[] args) throws ProtocolException, IOException, JAXBException {

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(String.format("%s\t%s\t%s", stdIn.readLine(), "searchString", "searchPmid"));
        String line;
        while((line = stdIn.readLine()) != null) {
            String reference = line.split("\t", 3)[1];
            String pmid = line.split("\t", 3)[2];
            Pattern pattern = Pattern.compile("[^\\.]*\\.");
            Matcher matcher = pattern.matcher(reference);
            String searchString = null;
            while(matcher.find()) {
                if(matcher.group().length() >= (searchString == null ? 0 : searchString.length())) {
                    searchString = matcher.group();
                }
            }
            List<Integer> pmidList = new ArrayList<>();
            if(searchString != null) {
                pmidList = getCitationPmid(searchString);
            }
            String searchPmid = null;
            if(pmidList.isEmpty()) {
                searchPmid = "NONE";
            }
            else if(pmidList.size() == 1) {
                searchPmid = pmidList.get(0).toString();
            }
            else {
                searchPmid = pmidList.toString();
            }
            System.out.println(String.format("%s\t%s\t%s", line, searchString, pmid != null && pmid.length() > 0 ? pmid : searchPmid));
        }
            
    }
    
    public static List<Integer> getCitationPmid(String search) throws ProtocolException, IOException, JAXBException {

        URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?field=title&term=" + URLEncoder.encode(search) + "&tool=CitationFinder.java&email=geoffrey.smith@emory.edu");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            if(!inputLine.startsWith("<!DOCTYPE")) {
                content.append(inputLine);
            }
        }
        in.close();

        con.disconnect();

        JAXBContext jc = JAXBContext.newInstance(ESearchResult.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ESearchResult eSearchResult = (ESearchResult)unmarshaller.unmarshal(new ByteArrayInputStream(content.toString().getBytes()));

        int count = 0;
        for(Object object : eSearchResult.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR()) {
            if(object instanceof Count) {
                count = Integer.parseInt(((Count)object).getvalue());
            }
        }
        
        List<Integer> pmidList = new ArrayList<>();
        for(Object object : eSearchResult.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR()) {
            if(object instanceof IdList) {
                for(Id id : ((IdList)object).getId()) {
                    pmidList.add(Integer.parseInt(id.getvalue()));
                }
            }
        }
        
        return pmidList;
        
    }
    
}
