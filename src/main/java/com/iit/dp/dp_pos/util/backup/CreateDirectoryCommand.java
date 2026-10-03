package com.iit.dp.dp_pos.util.backup;

import java.io.File;

/**
 * Command to create backup directory if it doesn't exist
 */
public class CreateDirectoryCommand implements BackupCommand {
    private final String directoryPath;

    public CreateDirectoryCommand(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public boolean execute() {
        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    System.out.println("Created backup directory: " + directoryPath);
                    return true;
                } else {
                    System.err.println("Failed to create backup directory: " + directoryPath);
                    return false;
                }
            } else {
                System.out.println("Backup directory already exists: " + directoryPath);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error creating directory: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Create backup directory at " + directoryPath;
    }
}
