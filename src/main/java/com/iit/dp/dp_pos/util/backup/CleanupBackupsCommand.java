package com.iit.dp.dp_pos.util.backup;

import java.io.File;

/**
 * Command to clean up old backup files, keeping only the specified number
 */
public class CleanupBackupsCommand implements BackupCommand {
    private final String backupDirectory;
    private final int maxBackupsToKeep;

    public CleanupBackupsCommand(String backupDirectory, int maxBackupsToKeep) {
        this.backupDirectory = backupDirectory;
        this.maxBackupsToKeep = maxBackupsToKeep;
    }

    @Override
    public boolean execute() {
        try {
            File backupDir = new File(backupDirectory);
            if (!backupDir.exists()) {
                System.out.println("Backup directory does not exist: " + backupDirectory);
                return true; // Not an error if directory doesn't exist
            }

            File[] backups = backupDir.listFiles((dir, name) -> 
                name.startsWith("pos_backup_") && name.endsWith(".db"));

            if (backups != null && backups.length > maxBackupsToKeep) {
                // Sort files by last modified time (oldest first)
                java.util.Arrays.sort(backups, 
                    (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

                // Delete oldest files until only maxBackupsToKeep remain
                int deletedCount = 0;
                for (int i = 0; i < backups.length - maxBackupsToKeep; i++) {
                    if (backups[i].delete()) {
                        deletedCount++;
                        System.out.println("Deleted old backup: " + backups[i].getName());
                    }
                }
                
                if (deletedCount > 0) {
                    System.out.println("Cleaned up " + deletedCount + " old backup(s)");
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to cleanup old backups: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Clean up old backups in " + backupDirectory + ", keeping only " + maxBackupsToKeep + " files";
    }
}
