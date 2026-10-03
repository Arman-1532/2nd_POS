package com.iit.dp.dp_pos.util.backup;

import java.util.ArrayList;
import java.util.List;

/**
 * Invoker class that executes backup commands
 */
public class BackupInvoker {
    private final List<BackupCommand> commandHistory;

    public BackupInvoker() {
        this.commandHistory = new ArrayList<>();
    }


    public boolean executeCommand(BackupCommand command) {
        System.out.println("Executing: " + command.getDescription());
        boolean result = command.execute();
        commandHistory.add(command);
        return result;
    }


    public boolean executeCommands(List<BackupCommand> commands) {
        boolean allSuccessful = true;
        for (BackupCommand command : commands) {
            if (!executeCommand(command)) {
                allSuccessful = false;
                System.err.println("Command failed: " + command.getDescription());
            }
        }
        return allSuccessful;
    }

    /**
     * Get command execution history
     */
    public List<BackupCommand> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    /**
     * Clear command history
     */
    public void clearHistory() {
        commandHistory.clear();
    }
}
