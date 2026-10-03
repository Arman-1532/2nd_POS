package com.iit.dp.dp_pos.report.actions;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class DownloadAction implements ReportActionStrategy {
    @Override
    public String getName() {
        return "Download";
    }

    @Override
    public void execute(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("Download");
        alert.setHeaderText(null);
        alert.setContentText("Downloaded successfully.");
        alert.showAndWait();
    }
}

