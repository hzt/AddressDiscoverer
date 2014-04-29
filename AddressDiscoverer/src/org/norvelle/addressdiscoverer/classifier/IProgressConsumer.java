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

import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public interface IProgressConsumer {
    
    public void reportProgressStage(ExtractIndividualsStatusReporter progress);
    
    public void reportText(String text);
    
}
