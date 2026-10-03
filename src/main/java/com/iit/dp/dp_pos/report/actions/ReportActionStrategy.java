package com.iit.dp.dp_pos.report.actions;

import javafx.stage.Stage;

public interface ReportActionStrategy {
    String getName();
    void execute(Stage owner);
}

