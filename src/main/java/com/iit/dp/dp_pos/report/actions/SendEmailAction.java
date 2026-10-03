package com.iit.dp.dp_pos.report.actions;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class SendEmailAction implements ReportActionStrategy {
    @Override
    public String getName() {
        return "Send Email";
    }

    @Override
    public void execute(Stage owner) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle("Send Email");
        dialog.setHeaderText("Enter recipient email address");
        dialog.setContentText("Email:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.initOwner(owner);
            ok.setTitle("Send Email");
            ok.setHeaderText(null);
            ok.setContentText("Email sent successfully to " + result.get().trim() + ".");
            ok.showAndWait();
        } else {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.initOwner(owner);
            warn.setTitle("Send Email");
            warn.setHeaderText(null);
            warn.setContentText("Email address is required.");
            warn.showAndWait();
        }
    }
}

