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
package org.norvelle.addressdiscoverer.individuals;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author enorvelle
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.norvelle.addressdiscoverer.individuals.OneRecPerTrMultipleTdsTwoLinks.class, 
    org.norvelle.addressdiscoverer.individuals.OneRecPerTrSingleTd.class, 
    org.norvelle.addressdiscoverer.individuals.OneRecPerTrMultiplePsInTd.class, 
    org.norvelle.addressdiscoverer.individuals.FindEmailInDetailPageTest.class, 
    org.norvelle.addressdiscoverer.individuals.OneRecPerTrMultipleTds.class,
    org.norvelle.addressdiscoverer.individuals.MultipleTdsMultipleRecordsPerTr.class,
    org.norvelle.addressdiscoverer.individuals.MultipleTdsForSingleRecordPerTrNoLinks.class,
    org.norvelle.addressdiscoverer.individuals.MultipleTdsForSingleRecordPerTrHrefLinks.class,
    org.norvelle.addressdiscoverer.individuals.SingleDivPerRecordWeblinks.class,
    org.norvelle.addressdiscoverer.individuals.SingleRecordPerDivWeblinks.class,
    org.norvelle.addressdiscoverer.individuals.MultipleDivsForSingleRecordPerDiv.class
})

public class TestAllIndividualPages { 
    
}
