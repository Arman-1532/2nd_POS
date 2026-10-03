package com.iit.dp.dp_pos.report.actions;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class PrintAction implements ReportActionStrategy {
    @Override
    public String getName() {
        return "Print";
    }

    @Override
    public void execute(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("Print");
        alert.setHeaderText(null);
        alert.setContentText("Printed successfully.");
        alert.showAndWait();
    }
}

