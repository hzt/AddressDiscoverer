/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package namedatabasescraper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageScraper {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private final List<String> names;
    private final String filename;
    private final String id;
    private final String dirname;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PageScraper(File file, String dirname, String selector, String charset) throws IOException {
        filename = file.getAbsolutePath();
        this.dirname = dirname;
        this.id = this.createScraperId();
        String html = FileUtils.readFileToString(file, charset);
        this.names = new ArrayList<>();
        Document soup = Jsoup.parse(html);
        //Elements nameElements = soup.select("a.nom");
        //Elements nameElements = soup.select("div > a:not(.n1)");
        Elements nameElements = soup.select(selector);
        for (Element nameElement : nameElements) {
            String name = nameElement.text();
            names.add(name);
        }
        logger.log(Level.INFO, "Scraped " + this.names.size() + " names from page {0}", file.getName());
    }
    
    public List<String> getNames() {
        return names;
    }

    public String getId() {
        return id;
    }
    
    public String createScraperId() {
        byte[] bytesOfMessage;
        try {
            bytesOfMessage = this.filename.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            NameDatabaseScraper.reportException(ex);
            return "";
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            NameDatabaseScraper.reportException(ex);
            return "";
        }
        byte[] thedigest = md.digest(bytesOfMessage);
        return thedigest.toString();
    }
    
    @Override
    public String toString() {
        return String.format("%d names extracted", this.names.size());
    }
  
    public String storeToCsv()  {
        StringBuilder builder = new StringBuilder();
        for (String name : this.names) 
            builder.append(name).append("\n");
        return builder.toString();
    }
    
    public void storeToDb(MainWindow parent) throws SQLException {
        logger.log(Level.INFO, "Started storing names for scraper id {0}", this.dirname);
        Connection conn = NameDatabaseScraper.application.getConnection();
        PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO names VALUES (?, ?)");
        stmt2.setString(2, this.dirname);
        conn.setAutoCommit(false);
        for (String name : this.names) {
            stmt2.setString(1, name);
            stmt2.addBatch();
        }
        stmt2.executeBatch();
        conn.setAutoCommit(true);
        logger.log(Level.INFO, "Stored " + this.names.size() + " names for scraper id {0}", this.dirname);
    }
}
