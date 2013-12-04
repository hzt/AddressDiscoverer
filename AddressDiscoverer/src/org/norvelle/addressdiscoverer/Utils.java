/*
 * Utilities for the ResearchAssistantJ project.
 */

package org.norvelle.addressdiscoverer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Utils class provides its methods as static so no instantiation is necessary.
 * 
 * @author Erik Norvelle
 */
public class Utils {
    
    public static String wordWrapString(String input, int maxLength) {
        List matchList = new ArrayList();
        Pattern regex = Pattern.compile("(.{1," + maxLength + "}(?:\\s|$))|(.{0," + maxLength + "})", Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
          matchList.add(regexMatcher.group());
        }
        return Utils.join(matchList, "\n");
    }
    
    public static String join(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";
        for(String s : list) {
            sb.append(loopDelim);
            sb.append(s);            
            loopDelim = delim;
        }
        return sb.toString();
    }
    
}
