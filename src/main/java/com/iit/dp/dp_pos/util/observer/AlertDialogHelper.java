package com.iit.dp.dp_pos.util.observer;

import com.iit.dp.dp_pos.model.Product;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;

public class AlertDialogHelper {

    private static boolean alertCurrentlyShowing = false; // Prevent multiple alerts

    public static void showProductAlerts(List<Product> outOfStockProducts, List<Product> expiredProducts) {
        // Quick check: if there's nothing to show, do nothing
        boolean hasOut = outOfStockProducts != null && !outOfStockProducts.isEmpty();
        boolean hasExp = expiredProducts != null && !expiredProducts.isEmpty();
        if (!hasOut && !hasExp) return;

        // Acquire lock and set the flag immediately to prevent multiple callers from scheduling
        synchronized (AlertDialogHelper.class) {
            if (alertCurrentlyShowing) return; // another caller already scheduled an alert
            alertCurrentlyShowing = true;
        }

        // Run the UI code on the JavaFX thread
        Platform.runLater(() -> {
            try {
                StringBuilder alertMessage = new StringBuilder();

                if (hasOut) {
                    alertMessage.append("⚠️ OUT OF STOCK PRODUCTS:\n");
                    for (Product product : outOfStockProducts) {
                        alertMessage.append("• ").append(product.getName())
                                  .append(" (ID: ").append(product.getId())
                                  .append(", Category: ").append(product.getCategory())
                                  .append(")\n");
                    }
                    alertMessage.append("\n");
                }

                if (hasExp) {
                    alertMessage.append("🗓️ EXPIRED PRODUCTS:\n");
                    for (Product product : expiredProducts) {
                        alertMessage.append("• ").append(product.getName())
                                  .append(" (ID: ").append(product.getId())
                                  .append(", Category: ").append(product.getCategory())
                                  .append(", Expired: ").append(product.getExpiryDate())
                                  .append(")\n");
                    }
                }

                if (alertMessage.length() > 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Product Alerts");
                    alert.setHeaderText("Attention Required!");
                    alert.setContentText(alertMessage.toString());

                    // When alert is closed, reset the flag in a thread-safe way
                    alert.setOnCloseRequest(e -> {
                        synchronized (AlertDialogHelper.class) {
                            alertCurrentlyShowing = false;
                        }
                    });

                    alert.showAndWait().ifPresent(response -> {
                        synchronized (AlertDialogHelper.class) {
                            alertCurrentlyShowing = false;
                        }
                    });
                } else {
                    // Nothing to show (shouldn't happen because of earlier check), reset flag
                    synchronized (AlertDialogHelper.class) {
                        alertCurrentlyShowing = false;
                    }
                }
            } catch (Throwable t) {
                // Ensure flag is cleared if something goes wrong
                synchronized (AlertDialogHelper.class) {
                    alertCurrentlyShowing = false;
                }
                t.printStackTrace();
            }
        });
    }

    // Reset the alert flag when needed
    public static void resetAlertFlag() {
        synchronized (AlertDialogHelper.class) {
            alertCurrentlyShowing = false;
        }
    }
}
