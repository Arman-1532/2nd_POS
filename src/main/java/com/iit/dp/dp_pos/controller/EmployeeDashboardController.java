package com.iit.dp.dp_pos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class EmployeeDashboardController {
    private MainController mainController;
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    @FXML
    public void onProductListClick(javafx.event.ActionEvent event) {
        mainController.loadView("product-list-view.fxml");
    }

    @FXML
    public void onCustomerListClick(javafx.event.ActionEvent event) {
        mainController.loadView("customer-list-view.fxml");
    }

    @FXML
    public void onOrderListClick(javafx.event.ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("order-history-view.fxml");
        }
    }

    @FXML
    public void onCreateSaleClick(javafx.event.ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("create-sale-view.fxml");
        }
    }

    @FXML
    public void onBackToLoginClick(javafx.event.ActionEvent event) {
        mainController.loadView("login-view.fxml");
    }

    @FXML
    public void onPausedOrdersClick(javafx.event.ActionEvent event) {
        mainController.loadView("paused-orders-view.fxml");
    }

    // private void showError(String title, String content) {
    //     Alert alert = new Alert(Alert.AlertType.ERROR);
    //     alert.setTitle(title);
    //     alert.setHeaderText(null);
    //     alert.setContentText(content);
    //     alert.showAndWait();
    // }
}
