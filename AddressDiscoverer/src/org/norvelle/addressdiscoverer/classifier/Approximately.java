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
public class Approximately {
    
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static int defaultRange = 10000000;
    
    public static boolean equals(int value1, int value2) {
        return equals(value1, value2, defaultRange);
    }
    
    public static boolean equals(int value1, int value2, int range) {
        double difference = ((double) Math.abs(value1 - value2)) / (double) range;
        logger.log(Level.INFO, String.format("Comparing %d to %d, difference = %f", value1, value2, difference));
        return difference < 0.15;
    }
    
    public static boolean equals(double value1, double value2) {
        return equals(value1, value2, defaultRange);
    }
    
    public static boolean equals(double value1, double value2, int range) {
        double difference = Math.abs(value1 - value2) / range;
        logger.log(Level.INFO, String.format("Comparing %f to %f, difference = %f", value1, value2, difference));
        return difference < 0.15;
    }
    
    
}
