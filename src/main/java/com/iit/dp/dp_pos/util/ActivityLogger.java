package com.iit.dp.dp_pos.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ActivityLogger {
    private static final String LOG_FILE = "activity_log.json";

    // Appends a log entry as a JSON object to the log file
    public static void log(String user, String action, String details) {
        try {
            Path logPath = Paths.get(LOG_FILE);
            String entry = String.format(
                "  {\"timestamp\": \"%s\", \"user\": \"%s\", \"action\": \"%s\", \"details\": \"%s\"}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                escape(user), escape(action), escape(details)
            );

            // If file doesn't exist, create with array brackets
            if (!Files.exists(logPath)) {
                try (BufferedWriter writer = Files.newBufferedWriter(logPath)) {
                    writer.write("[\n" + entry + "\n]");
                }
            } else {
                // Insert before the last ']' to keep a valid array
                RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "rw");
                long length = raf.length();
                if (length > 2) {
                    raf.seek(length - 2); // before \n]
                    raf.writeBytes(",\n" + entry + "\n]");
                } else {
                    raf.seek(0);
                    raf.writeBytes("[\n" + entry + "\n]");
                }
                raf.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
    public static String getEmailByUserId(int userId) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT email FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            System.err.println("Failed to get user email: " + e.getMessage());
        }
        return "unknown";
    }


    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
