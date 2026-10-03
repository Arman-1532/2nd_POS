package com.iit.dp.dp_pos.util.backup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composite command that executes multiple backup commands as a single unit
 */
public class CompositeBackupCommand implements BackupCommand {
    private final List<BackupCommand> commands;
    private final String description;

    public CompositeBackupCommand(String description, BackupCommand... commands) {
        this.description = description;
        this.commands = new ArrayList<>(Arrays.asList(commands));
    }

    public CompositeBackupCommand(String description, List<BackupCommand> commands) {
        this.description = description;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public boolean execute() {
        boolean allSuccessful = true;
        System.out.println("Executing composite command: " + description);
        
        for (BackupCommand command : commands) {
            if (!command.execute()) {
                allSuccessful = false;
                System.err.println("Failed to execute: " + command.getDescription());
            }
        }
        
        if (allSuccessful) {
            System.out.println("Composite command completed successfully: " + description);
        } else {
            System.err.println("Composite command completed with errors: " + description);
        }
        
        return allSuccessful;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Add a command to the composite
     */
    public void addCommand(BackupCommand command) {
        commands.add(command);
    }

    /**
     * Remove a command from the composite
     */
    public void removeCommand(BackupCommand command) {
        commands.remove(command);
    }

    /**
     * Get all commands in this composite
     */
    public List<BackupCommand> getCommands() {
        return new ArrayList<>(commands);
    }
}
