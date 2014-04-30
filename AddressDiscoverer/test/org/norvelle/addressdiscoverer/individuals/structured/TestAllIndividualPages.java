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
package org.norvelle.addressdiscoverer.individuals.structured;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author enorvelle
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.norvelle.addressdiscoverer.individuals.structured.OneRecPerTrMultipleTdsTwoLinks.class, 
    org.norvelle.addressdiscoverer.individuals.structured.OneRecPerTrSingleTd.class, 
    org.norvelle.addressdiscoverer.individuals.structured.OneRecPerTrMultiplePsInTd.class, 
    org.norvelle.addressdiscoverer.individuals.structured.FindEmailInDetailPageTest.class, 
    org.norvelle.addressdiscoverer.individuals.structured.OneRecPerTrMultipleTds.class,
    org.norvelle.addressdiscoverer.individuals.structured.MultipleTdsMultipleRecordsPerTr.class,
    org.norvelle.addressdiscoverer.individuals.structured.MultipleTdsForSingleRecordPerTrNoLinks.class,
    org.norvelle.addressdiscoverer.individuals.structured.MultipleTdsForSingleRecordPerTrHrefLinks.class,
    org.norvelle.addressdiscoverer.individuals.structured.SingleDivPerRecordWeblinks.class,
    org.norvelle.addressdiscoverer.individuals.structured.SingleRecordPerDivWeblinks.class,
    org.norvelle.addressdiscoverer.individuals.structured.SingleRecordPerLiWeblinks.class,
    org.norvelle.addressdiscoverer.individuals.structured.MultipleDivsForSingleRecordPerDiv.class
})

public class TestAllIndividualPages { 
    
}
