package com.iit.dp.dp_pos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.iit.dp.dp_pos.util.UserSession;

import java.io.IOException;
import java.util.function.Consumer;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Label titleLabel;
    @FXML private Label userInfoLabel;
    
    private Stage stage;
    private String currentView = ""; // Track the current view

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    public void initialize() {
        // Start with login view
        loadView("login-view.fxml");
    }
    
    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iit/dp/dp_pos/" + fxmlFile));
            Parent view = loader.load();

            // Set the main controller reference in child controllers
            Object controller = loader.getController();

            if (controller instanceof AuthController) {
                ((AuthController) controller).setMainController(this);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setMainController(this);
            } else if (controller instanceof EmployeeDashboardController) {
                ((EmployeeDashboardController) controller).setMainController(this);
            } else if (controller instanceof ProductListController) {
                ((ProductListController) controller).setMainController(this);
            } else if (controller instanceof CustomerListController) {
                ((CustomerListController) controller).setMainController(this);
            } else if (controller instanceof OrderHistoryController) {
                ((OrderHistoryController) controller).setMainController(this);
            } else if (controller instanceof CreateSaleController) {
                ((CreateSaleController) controller).setMainController(this);
            } else if (controller instanceof ManageProductsController) {
                ManageProductsController manageController = (ManageProductsController) controller;
                manageController.setMainController(this);
                // Set the correct return view based on where we came from
                String returnView = determineReturnView();
                manageController.setReturnView(returnView);
            } else if (controller instanceof PausedOrdersController) {
                ((PausedOrdersController) controller).setMainController(this);
            }
            // Track the current view before switching
            currentView = fxmlFile;
            // Clear and set new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            // Update title based on view
            updateTitle(fxmlFile);
        } catch (IOException e) {
            System.err.println("[MainController] Failed to load view: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[MainController] Unexpected error loading view: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New overload that allows configuring the child controller before showing the view
    public void loadView(String fxmlFile, Consumer<Object> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iit/dp/dp_pos/" + fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();

            if (controller instanceof AuthController) {
                ((AuthController) controller).setMainController(this);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setMainController(this);
            } else if (controller instanceof EmployeeDashboardController) {
                ((EmployeeDashboardController) controller).setMainController(this);
            } else if (controller instanceof ProductListController) {
                ((ProductListController) controller).setMainController(this);
            } else if (controller instanceof CustomerListController) {
                ((CustomerListController) controller).setMainController(this);
            } else if (controller instanceof OrderHistoryController) {
                ((OrderHistoryController) controller).setMainController(this);
            } else if (controller instanceof CreateSaleController) {
                ((CreateSaleController) controller).setMainController(this);
            } else if (controller instanceof ManageProductsController) {
                ManageProductsController manageController = (ManageProductsController) controller;
                manageController.setMainController(this);
                String returnView = determineReturnView();
                manageController.setReturnView(returnView);
            } else if (controller instanceof PausedOrdersController) {
                ((PausedOrdersController) controller).setMainController(this);
            }

            // Allow caller to configure the controller (e.g., inject a paused sale)
            if (controllerConfigurer != null) {
                try {
                    controllerConfigurer.accept(controller);
                } catch (Exception cfgEx) {
                    System.err.println("[MainController] Controller configuration failed for " + fxmlFile + ": " + cfgEx.getMessage());
                    cfgEx.printStackTrace();
                }
            }

            currentView = fxmlFile;
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            updateTitle(fxmlFile);
        } catch (IOException e) {
            System.err.println("[MainController] Failed to load view: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[MainController] Unexpected error loading view: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determine which view to return to from ManageProducts based on current context
     */
    private String determineReturnView() {
        // Check if we're coming from admin dashboard or employee dashboard
        // We can determine this by checking the current view or user role

        // If the current view is admin dashboard (hello-view), return to admin
        if ("hello-view.fxml".equals(currentView)) {
            return "hello-view.fxml";
        }
        // If current view is employee dashboard, return to employee
        else if ("employee-dashboard-view.fxml".equals(currentView)) {
            return "employee-dashboard-view.fxml";
        }
        // Default to employee dashboard for backward compatibility
        else {
            return "employee-dashboard-view.fxml";
        }
    }

    private void updateTitle(String fxmlFile) {
        switch (fxmlFile) {
            case "login-view.fxml":
                titleLabel.setText("Login - Supershop POS System");
                break;
            case "hello-view.fxml":
                titleLabel.setText("Admin Dashboard - Supershop POS System");
                break;
            case "employee-dashboard-view.fxml":
                titleLabel.setText("Employee Dashboard - Supershop POS System");
                break;
            case "product-list-view.fxml":
                titleLabel.setText("Product List - Supershop POS System");
                break;
            case "customer-list-view.fxml":
                titleLabel.setText("Customer List - Supershop POS System");
                break;
            case "order-history-view.fxml":
                titleLabel.setText("Order History - Supershop POS System");
                break;
            case "create-sale-view.fxml":
                titleLabel.setText("Create Sale - Supershop POS System");
                break;
            case "manage-products-view.fxml":
                titleLabel.setText("Manage Products - Supershop POS System");
                break;
            case "paused-orders-view.fxml":
                titleLabel.setText("Paused Orders - Supershop POS System");
                break;
            default:
                titleLabel.setText("Supershop POS System");
        }
    }
    
    public void updateUserInfo(String userInfo) {
        userInfoLabel.setText(userInfo);
    }
    
    @FXML
    private void onLogoutClick() {
        // Clear user session
        AuthController.loggedInUserId = -1;
        // Load login view
        loadView("login-view.fxml");
        updateUserInfo("Welcome");
    }
}
