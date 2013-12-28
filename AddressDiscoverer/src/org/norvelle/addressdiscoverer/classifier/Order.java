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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class Order {
    
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static int defaultRange = 10000000;
    
    public static boolean of(int value1, int value2) {
        return of((double) value1, (double) value2);
    }
        
    public static boolean of(double value1, double value2) {
        double highest = Math.max(value1, value2);
        double highestOverFour = highest / 4;
        return (value1 > highestOverFour && value2 > highestOverFour);
    }
    
    
}
