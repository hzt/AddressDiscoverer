/**
 *  Part of the AddressDiscoverer project, licensed under the GPL v.2 license. 
 *  This project provides intelligence for discovering email addresses in 
 *  specified web pages, associating them with a given institution and department 
 *  and address type.
 */

package org.norvelle.addressdiscoverer.model;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a single institution. More or less just is a list of departments,
 * with methods or adding and deleting departments.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */

public class Institution {
    
    private final HashMap<String, Department> departments = new HashMap();
    private String name;
    private String id;
    
    /**
     * Initialize the institution with a name, plus a unique id.
     * 
     * @param name 
     */
    public Institution(String name) {
        this.name = name;
        this.id = UUID.randomUUID().toString();
    }
}
