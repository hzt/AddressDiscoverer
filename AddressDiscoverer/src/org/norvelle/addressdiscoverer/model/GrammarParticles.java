/**
 * Part of the AddressDiscoverer project, licensed under the GPL v.3 license.
 * This project provides intelligence for discovering email addresses in
 * specified web pages, associating them with a given firstName and department
 * and address type.
 *
 * This project is licensed under the GPL v.3. Your rights to copy and modify
 * are regulated by the conditions specified in that license, available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 */
package org.norvelle.addressdiscoverer.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an access pathway to our list of grammar particles
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class GrammarParticles {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first grammarParticles
    private static final List<String> grammarParticles = new ArrayList<>();
    
    // The file where we store our last grammarParticles
    private static File grammarParticlesFile;
        
    // ===================== Static Data Manipulation Methods =============================
    
    @SuppressWarnings("ManualArrayToCollectionCopy")
    public static void initialize(String settingsDir) throws IOException {
        grammarParticlesFile = new File(settingsDir + File.separator + "particles.txt");
        String particleStr = FileUtils.readFileToString(grammarParticlesFile, "UTF-8");
        String[] grammarParticlesArray = StringUtils.split(particleStr, "\n");
        for (String particle : grammarParticlesArray) 
            grammarParticles.add(particle.trim());
    }
    
    public static void store() throws IOException {
        throw new UnsupportedOperationException(); 
        //String grammarParticlesStr = StringUtils.join(grammarParticles.keySet(), "\n");
        //FileUtils.writeStringToFile(grammarParticlesFile, grammarParticlesStr, "UTF-8");
    }
    
    public static boolean isGrammarParticle(String name) {
        // Instead of using a standard charset translator, we translate only vowels
        for (String particle : grammarParticles)
            if (particle.equalsIgnoreCase(name))
                return true;
        return false;
    }

    public static void delete(String abbreviation) {
        throw new UnsupportedOperationException(); 
        //if (grammarParticles.containsKey(abbreviation))
        //    grammarParticles.remove(abbreviation);
    }

    public static void add(String abbreviation) {
        throw new UnsupportedOperationException(); 
        //grammarParticles.put(abbreviation, 1);
    }

}
