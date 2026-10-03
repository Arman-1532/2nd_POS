package com.iit.dp.dp_pos;

import com.iit.dp.dp_pos.util.backup.DatabaseBackupUtil;
import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database connection singleton
        try {
            DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
            dbManager.getConnection(); // Initialize connection
            dbManager.initializeSchema(); // Add category column if it doesn't exist
            com.iit.dp.dp_pos.util.DatabaseConnectionManager.getInstance().ensureDefaultAdminUser();
        } catch (Exception e) {
            // Silent handling
        }

        // Initialize database backup system
        DatabaseBackupUtil.startAutomaticBackup();

        FXMLLoader fxmlLoader = new FXMLLoader(
                Application.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // Set the stage on the MainController
        com.iit.dp.dp_pos.controller.MainController mainController = fxmlLoader.getController();
        mainController.setStage(stage);
        stage.setTitle("Supershop App");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        try {
            // Clean up backup service when application closes
            DatabaseBackupUtil.stopBackupService();
            
            // Close database connection
            DatabaseConnectionManager.getInstance().closeConnection();
            
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}