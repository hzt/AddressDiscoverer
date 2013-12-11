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
package org.norvelle.addressdiscoverer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.norvelle.addressdiscoverer.model.NameTest;
import org.norvelle.addressdiscoverer.parser.NameChunkTest;
import org.norvelle.addressdiscoverer.parser.EmailElementFinderTest;
import org.norvelle.addressdiscoverer.parser.JSoupTest;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    EmailElementFinderTest.class, 
    NameChunkTest.class,
    NameTest.class,
    JSoupTest.class
})

public class AddressDiscovererTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
