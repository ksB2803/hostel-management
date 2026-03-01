package com.kripal.hostel.service;

import com.kripal.hostel.exception.DatabaseException;
import com.kripal.hostel.util.DBConnection;
import com.kripal.hostel.util.LogUtil;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;

public class XMLImportService {

    private static final String INSERT_SQL =
            "INSERT INTO STUDENT (roll_no, name, branch, year, cgpa, attendance, email, gender, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE') " +
            "ON CONFLICT (roll_no) DO NOTHING";

    public int importFolder(String folderPath, String username) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("[XML] Folder not found: " + folderPath); return 0;
        }
        File[] xmlFiles = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            System.out.println("[XML] No XML files found in: " + folderPath); return 0;
        }
        System.out.printf("[XML] Found %d XML files%n", xmlFiles.length);
        int total = 0;
        for (File f : xmlFiles) {
            System.out.print("  Importing " + f.getName() + " ... ");
            int c = importFile(f.getAbsolutePath(), username);
            System.out.println(c + " students");
            total += c;
        }
        System.out.printf("[XML] Done. Total: %d%n", total);
        LogUtil.log("XML folder import: " + total + " students", username);
        return total;
    }

    public int importFile(String path, String username) {
        File file = new File(path);
        if (!file.exists()) { System.out.println("[XML] File not found: " + path); return 0; }

        Document doc;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            doc = f.newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new DatabaseException("XML parse failed: " + e.getMessage(), e);
        }

        NodeList nodes = doc.getElementsByTagName("student");
        if (nodes.getLength() == 0) return 0;

        Connection conn = DBConnection.getInstance().getConnection();
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            conn.setAutoCommit(false);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el     = (Element) nodes.item(i);
                String rollNo  = getText(el, "roll_no");
                String name    = getText(el, "n");
                String branch  = getText(el, "branch");
                String year    = getText(el, "year");
                String cgpa    = getText(el, "cgpa");
                String att     = getText(el, "attendance");
                String email   = getText(el, "email");
                String gender  = getText(el, "gender");

                if (rollNo.isEmpty() || name.isEmpty()) continue;

                ps.setString    (1, rollNo);
                ps.setString    (2, name);
                ps.setString    (3, branch.isEmpty() ? null : branch);
                ps.setString    (4, year.isEmpty()   ? null : year);
                ps.setBigDecimal(5, cgpa.isEmpty()   ? BigDecimal.ZERO : new BigDecimal(cgpa));
                ps.setBigDecimal(6, att.isEmpty()    ? BigDecimal.ZERO : new BigDecimal(att));
                ps.setString    (7, email.isEmpty()  ? null : email);
                ps.setString    (8, gender.isEmpty() ? "M"  : gender.toUpperCase());
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new DatabaseException("Batch insert failed: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
        return count;
    }

    private String getText(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        Node n = nl.item(0).getFirstChild();
        return n != null ? n.getNodeValue().trim() : "";
    }
}
