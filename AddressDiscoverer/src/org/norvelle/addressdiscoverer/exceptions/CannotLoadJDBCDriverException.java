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
package org.norvelle.addressdiscoverer.exceptions;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class CannotLoadJDBCDriverException extends Exception {

    /**
     * Creates a new instance of <code>CannotLoadJDBCDriverException</code>
     * without detail message.
     */
    public CannotLoadJDBCDriverException() {
    }

    /**
     * Constructs an instance of <code>CannotLoadJDBCDriverException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public CannotLoadJDBCDriverException(String msg) {
        super("Could not load SQLite JDBC driver: " + msg);
    }
}
