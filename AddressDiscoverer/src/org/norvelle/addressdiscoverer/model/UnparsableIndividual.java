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
package org.norvelle.addressdiscoverer.model;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnparsableIndividual extends Individual {

    @Override
    public String getEmail() {
        return "Unparsable"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLastName() {
        return "Unparsable"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFirstName() {
        return "Unparsable"; //To change body of generated methods, choose Tools | Templates.
    }
    
    String id;
    
    public UnparsableIndividual(String identifier) {
        this.id = identifier;
    }
    
    @Override
    public String toString() {
        if (this.id.trim().isEmpty())
            return "Empty data given";
        return "Unparsed: " + this.id;
    }
    
}
