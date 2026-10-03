package com.iit.dp.dp_pos.report;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import com.iit.dp.dp_pos.report.actions.DownloadAction;
import com.iit.dp.dp_pos.report.actions.PrintAction;
import com.iit.dp.dp_pos.report.actions.ReportActionStrategy;
import com.iit.dp.dp_pos.report.actions.SendEmailAction;

public class VisualReportGenerator implements SalesReportGenerator {
    private String reportFormat = "VISUAL"; // default

    public void setReportFormat(String reportFormat) {
        if (reportFormat != null && !reportFormat.isBlank()) {
            this.reportFormat = reportFormat.toUpperCase();
        }
    }

    @Override
    public List<SalesReportRow> generateReport(LocalDate start, LocalDate end) {
        // Use TableSalesReportGenerator to actually fetch the data
        TableSalesReportGenerator tableGenerator = new TableSalesReportGenerator();
        return tableGenerator.generateReport(start, end);
    }

    @Override
    public boolean export(List<SalesReportRow> rows, LocalDate start, LocalDate end) {
        try {
            // Create a new window to display the report
            Stage reportStage = new Stage();
            String title = String.format("%s Report%s%s",
                    reportFormat,
                    (start != null ? " from " + start : ""),
                    (end != null ? " to " + end : ""));
            reportStage.setTitle(title);

            // Create table view
            TableView<SalesReportRow> tableView = new TableView<>();
            tableView.setItems(FXCollections.observableArrayList(rows));

            // Configure columns
            TableColumn<SalesReportRow, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn<SalesReportRow, String> customerNameCol = new TableColumn<>("Customer Name");
            customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

            TableColumn<SalesReportRow, String> customerEmailCol = new TableColumn<>("Customer Email");
            customerEmailCol.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));

            TableColumn<SalesReportRow, String> productNameCol = new TableColumn<>("Product");
            productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

            TableColumn<SalesReportRow, Double> buyingPriceCol = new TableColumn<>("Unit Buying Price");
            buyingPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitBuyingPrice"));
            buyingPriceCol.setCellFactory(col -> new TableCell<SalesReportRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                }
            });

            TableColumn<SalesReportRow, Double> sellingPriceCol = new TableColumn<>("Unit Selling Price");
            sellingPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            sellingPriceCol.setCellFactory(col -> new TableCell<SalesReportRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                }
            });

            TableColumn<SalesReportRow, Integer> quantityCol = new TableColumn<>("Quantity");
            quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            TableColumn<SalesReportRow, Double> totalPriceCol = new TableColumn<>("Total Selling Price");
            totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
            totalPriceCol.setCellFactory(col -> new TableCell<SalesReportRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                }
            });

            TableColumn<SalesReportRow, Double> profitCol = new TableColumn<>("Profit");
            profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
            profitCol.setCellFactory(col -> new TableCell<SalesReportRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                }
            });

            // Add columns to the table
            tableView.getColumns().addAll(
                dateCol, customerNameCol, customerEmailCol, productNameCol,
                quantityCol, buyingPriceCol, sellingPriceCol, totalPriceCol, profitCol
            );

            // Set column widths
            dateCol.setPrefWidth(100);
            customerNameCol.setPrefWidth(150);
            customerEmailCol.setPrefWidth(200);
            productNameCol.setPrefWidth(150);
            quantityCol.setPrefWidth(80);
            buyingPriceCol.setPrefWidth(120);
            sellingPriceCol.setPrefWidth(120);
            totalPriceCol.setPrefWidth(120);
            profitCol.setPrefWidth(100);

            // Calculate totals
            double totalSales = 0;
            double totalProfit = 0;
            int totalItems = 0;

            for (SalesReportRow row : rows) {
                totalSales += row.getTotalPrice();
                totalProfit += row.getProfit();
                totalItems += row.getQuantity();
            }

            // Create summary labels
            Label header = new Label(title);
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Label summaryLabel = new Label("Report Summary");
            summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label totalItemsLabel = new Label(String.format("Total Items: %d", totalItems));
            Label totalSalesLabel = new Label(String.format("Total Sales: $%.2f", totalSales));
            Label totalProfitLabel = new Label(String.format("Total Profit: $%.2f", totalProfit));

            // Strategy buttons
            List<ReportActionStrategy> strategies = Arrays.asList(
                new DownloadAction(),
                new PrintAction(),
                new SendEmailAction()
            );
            HBox actionsBox = new HBox(10);
            actionsBox.setPadding(new Insets(5, 0, 0, 0));
            for (ReportActionStrategy strategy : strategies) {
                Button btn = new Button(strategy.getName());
                btn.setOnAction(e -> strategy.execute(reportStage));
                actionsBox.getChildren().add(btn);
            }

            // Create layout
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(
                header,
                tableView,
                summaryLabel,
                totalItemsLabel,
                totalSalesLabel,
                totalProfitLabel,
                actionsBox
            );

            // Set scene
            Scene scene = new Scene(layout, 1200, 620);
            reportStage.setScene(scene);
            reportStage.show();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFormatName() {
        return "VISUAL";
    }
}
