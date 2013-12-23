/**
 * Part of the AddressDiscoverer project, licensed under the GPL v.3 license.
 * This project provides intelligence for discovering email addresses in
 * specified web pages, associating them with a given institution and department
 * and address type.
 *
 * This project is licensed under the GPL v.3. Your rights to copy and modify
 * are regulated by the conditions specified in that license, available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 */
package org.norvelle.addressdiscoverer.classifier;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class Approximately {
    
    public static boolean equals(int value1, int value2) {
        if (value1 == 0 || value2 == 0)
            return false;
        double percentDifference = Math.abs(value1 - value2) / Math.max(value1, value2);
        return percentDifference > 0.9;
    }
    
    public static boolean equals(int value1, int[] values2) {
        for (int value2 : values2) {
            if (Approximately.equals(value1, value2))
                return true;
        }
        return false;
    }
    
    public static boolean equals(double value1, double value2) {
        if (value1 == 0.0 || value2 == 0.0)
            return false;
        double percentDifference = Math.abs(value1 - value2) / Math.max(value1, value2);
        return percentDifference > 0.9;
    }
    
    public static boolean equals(double value1, double[] values2) {
        for (double value2 : values2) {
            if (Approximately.equals(value1, value2))
                return true;
        }
        return false;
    }
    
}
