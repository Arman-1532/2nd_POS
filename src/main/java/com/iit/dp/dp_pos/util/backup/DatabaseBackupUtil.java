package com.iit.dp.dp_pos.util.backup;

import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Unified database backup utility that combines backup operations and automatic scheduling
 */
public class DatabaseBackupUtil {
    private static final String DB_PATH = "pos.db";
    private static final String BACKUP_DIR = "database_backups";
    private static final int MAX_BACKUPS = 5;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final BackupInvoker backupInvoker = new BackupInvoker();

    public static void startAutomaticBackup() {
        System.out.println("Starting automatic database backup system using Command Pattern...");

        // Initialize backup directory
        BackupCommand initCommand = createDirectoryCommand();
        backupInvoker.executeCommand(initCommand);

        // Schedule automatic backups every 6 hours
        scheduler.scheduleAtFixedRate(() -> {
            CompositeBackupCommand backupCommand = createCompleteBackupCommand();
            boolean success = backupInvoker.executeCommand(backupCommand);
            
            if (success) {
                System.out.println("Automatic backup completed successfully at " + LocalDateTime.now());
            } else {
                System.err.println("Automatic backup failed at " + LocalDateTime.now());
            }
        }, 0, 6, TimeUnit.HOURS);
    }

    // Method to manually create a backup for testing
//    public static void createManualBackup() {
//        System.out.println("Creating manual backup using Command Pattern...");
//        CompositeBackupCommand backupCommand = createCompleteBackupCommand();
//        boolean success = backupInvoker.executeCommand(backupCommand);
//
//        if (success) {
//            System.out.println("Manual backup completed successfully");
//        } else {
//            System.err.println("Manual backup failed");
//        }
//    }

    // Method to create a simple backup without cleanup
//    public static void createSimpleBackup() {
//        System.out.println("Creating simple backup using Command Pattern...");
//        CompositeBackupCommand simpleBackupCommand = createSimpleBackupCommand();
//        boolean success = backupInvoker.executeCommand(simpleBackupCommand);
//
//        if (success) {
//            System.out.println("Simple backup completed successfully");
//        } else {
//            System.err.println("Simple backup failed");
//        }
//    }

    // Method to clean up old backups only
//    public static void cleanupOldBackups() {
//        System.out.println("Cleaning up old backups using Command Pattern...");
//        CleanupBackupsCommand cleanupCommand = new CleanupBackupsCommand(BACKUP_DIR, MAX_BACKUPS);
//        boolean success = backupInvoker.executeCommand(cleanupCommand);
//
//        if (success) {
//            System.out.println("Cleanup completed successfully");
//        } else {
//            System.err.println("Cleanup failed");
//        }
//    }

    public static void stopBackupService() {
        System.out.println("Stopping backup service...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                System.out.println("Backup service forcibly stopped");
            } else {
                System.out.println("Backup service stopped gracefully");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            System.err.println("Backup service shutdown interrupted");
        }
    }

    // Method to get backup status and list existing backups
//    public static void printBackupStatus() {
//        File backupDir = new File(BACKUP_DIR);
//        if (!backupDir.exists()) {
//            System.out.println("Backup directory does not exist");
//            return;
//        }
//
//        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("pos_backup_") && name.endsWith(".db"));
//        if (backups == null || backups.length == 0) {
//            System.out.println("No backup files found");
//        } else {
//            System.out.println("Found " + backups.length + " backup file(s):");
//            java.util.Arrays.sort(backups, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
//            for (File backup : backups) {
//                System.out.println("  - " + backup.getName() + " (Size: " + backup.length() + " bytes)");
//            }
//        }
//    }
//
//    // Method to get command execution history
//    public static void printCommandHistory() {
//        System.out.println("Backup Command Execution History:");
//        var history = backupInvoker.getCommandHistory();
//        if (history.isEmpty()) {
//            System.out.println("No commands executed yet");
//        } else {
//            for (int i = 0; i < history.size(); i++) {
//                System.out.println((i + 1) + ". " + history.get(i).getDescription());
//            }
//        }
//    }
//
//    // Method to clear command history
//    public static void clearCommandHistory() {
//        backupInvoker.clearHistory();
//        System.out.println("Command history cleared");
//    }

    /**
     * Create a backup directory command - simplified factory method
     */
    private static CreateDirectoryCommand createDirectoryCommand() {
        return new CreateDirectoryCommand(BACKUP_DIR);
    }

    /**
     * Create a complete backup command - simplified factory method
     */
    private static CompositeBackupCommand createCompleteBackupCommand() {
        CreateDirectoryCommand createDirCommand = new CreateDirectoryCommand(BACKUP_DIR);
        CreateBackupCommand createBackupCommand = new CreateBackupCommand(DB_PATH, BACKUP_DIR);
        CleanupBackupsCommand cleanupCommand = new CleanupBackupsCommand(BACKUP_DIR, MAX_BACKUPS);

        return new CompositeBackupCommand(
            "Complete database backup process",
            createDirCommand,
            createBackupCommand,
            cleanupCommand
        );
    }

    /**
     * Create a simple backup command - simplified factory method
     */
//    private static CompositeBackupCommand createSimpleBackupCommand() {
//        CreateDirectoryCommand createDirCommand = new CreateDirectoryCommand(BACKUP_DIR);
//        CreateBackupCommand createBackupCommand = new CreateBackupCommand(DB_PATH, BACKUP_DIR);
//
//        return new CompositeBackupCommand(
//            "Simple database backup",
//            createDirCommand,
//            createBackupCommand
//        );
//    }
}
