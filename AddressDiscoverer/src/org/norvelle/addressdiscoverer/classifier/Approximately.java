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
    
    public static int range = 10000000;
    
    public static boolean equals(int value1, int value2) {
        double difference = (Math.abs(value1 - value2) / Math.max(value1, value2)) / range;
        return difference < 0.1;
    }
    
    public static boolean equals(double value1, double value2) {
        double difference = (Math.abs(value1 - value2) / Math.max(Math.abs(value1), Math.abs(value2))) / range;
        return difference < 0.1;
    }
    
    
}
