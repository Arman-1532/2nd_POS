module com.iit.dp.dp_pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;

    opens com.iit.dp.dp_pos to javafx.fxml;
    exports com.iit.dp.dp_pos;

    opens com.iit.dp.dp_pos.controller to javafx.fxml;
    exports com.iit.dp.dp_pos.controller;

    opens com.iit.dp.dp_pos.model to javafx.base;

    opens com.iit.dp.dp_pos.report to javafx.base;
}