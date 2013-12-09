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
    
    List<String> names;
    String filename;
    String id;
    
    public PageScraper(File file) throws IOException {
        filename = file.getAbsolutePath();
        this.id = this.createScraperId();
        String html = FileUtils.readFileToString(file);
        this.names = new ArrayList<>();
        Document soup = Jsoup.parse(html);
        Elements nameElements = soup.select("a.nom");
        for (Element nameElement : nameElements) {
            String name = nameElement.ownText();
            names.add(name);
        }
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
    
    public void storeToDb() throws SQLException {
        Connection conn = NameDatabaseScraper.application.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM names WHERE id = ?");
        stmt.setString(0, this.id);
        stmt.execute();
        
        PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO names VALUES (?, ?)");
        stmt2.setString(1, this.id);
        for (String name : this.names) {
            stmt2.setString(0, name);
            stmt2.execute();
        }
    }
}
