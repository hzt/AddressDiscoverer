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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameElementPath {
    
    private List<String> path = new ArrayList<>();
    private String signature;
    
    public NameElementPath(NameElement nm) {
        this.path = nm.getPathToContainer();
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(NameElementPath.class.getName()).log(Level.SEVERE, null, ex);
            this.signature = "";
            return;
        }
        for (String pathPart : this.path)
            m.update(pathPart.getBytes());
        this.signature = m.toString();
    }
    
    public String getSignature() {
        return this.signature;
    }
 
    public String toString() {
        String pathString = StringUtils.join(this.path, " / ");
        return pathString;
    }
    
    public String getJsoupPath() {
        String pathString = StringUtils.join(this.path, " > ");
        return pathString;
    }
}
