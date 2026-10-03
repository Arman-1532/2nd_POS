package com.iit.dp.dp_pos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AdminDashboardController {
    private MainController mainController;

    @FXML
    private void onAddProductClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("manage-products-view.fxml");
        }
    }

    @FXML
    private void onProductListClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("product-list-view.fxml");
        }
    }

    @FXML
    private void onCustomerListClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("customer-list-view.fxml");
        }
    }

    @FXML
    private void onOrderListClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("order-history-view.fxml");
        }
    }

    @FXML
    private void onCreateSaleClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("create-sale-view.fxml");
        }
    }

    @FXML
    private void onBackToLoginClick(ActionEvent event) {
        if (mainController != null) {
            mainController.loadView("login-view.fxml");
            mainController.updateUserInfo("Welcome");
        }
    }

    public void updateUserInfo(String info) {
        System.out.println("[AdminDashboardController] " + info);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
