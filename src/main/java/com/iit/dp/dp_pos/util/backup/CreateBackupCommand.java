package com.iit.dp.dp_pos.util.backup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Command to create a database backup
 */
public class CreateBackupCommand implements BackupCommand {
    private final String sourcePath;
    private final String backupDirectory;
    private String backupFilePath;

    public CreateBackupCommand(String sourcePath, String backupDirectory) {
        this.sourcePath = sourcePath;
        this.backupDirectory = backupDirectory;
    }

    @Override
    public boolean execute() {
        try {
            // Check if source database exists
            File sourceDb = new File(sourcePath);
            if (!sourceDb.exists()) {
                System.err.println("Source database file not found: " + sourcePath);
                return false;
            }

            // Generate timestamp for backup file name
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);

            // Create backup file path
            backupFilePath = backupDirectory + "/pos_backup_" + timestamp + ".db";

            // Copy the database file to backup location
            Files.copy(Paths.get(sourcePath), Paths.get(backupFilePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database backup created: " + backupFilePath);
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Create database backup from " + sourcePath + " to " + backupDirectory;
    }

    public String getBackupFilePath() {
        return backupFilePath;
    }
}
