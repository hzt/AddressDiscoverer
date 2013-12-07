/*
 * Copyright (C) 2013 Erik Norvelle <erik.norvelle@cyberlogos.co>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package regex;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class TestRegex {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String result = "<a style=\"color: rgb(65,94,161)\" href=\"mailto:ccastillog@unav.es\">\n" +
            " <strong>ccastillog@unav.es</strong>\n" +
            " </a>";
        Pattern pattern = Pattern.compile("unav\\.es");
        System.out.println("Pattern = " + pattern.toString());
        Matcher matcher;
        matcher = pattern.matcher(result);
        if (!matcher.find())
            System.out.println("Could not match pattern");
        else {
            int groupsFound = matcher.groupCount();
            for (int i = 0; i <= groupsFound ; i++) {
                String resultString = matcher.group(i);
                System.out.println("Found result = " + resultString);
            }
        }
    }

}

