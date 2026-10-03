package com.iit.dp.dp_pos.report;

public class SalesReportRow {
    private String date;
    private String customerName;
    private String customerEmail;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double unitBuyingPrice;
    private double totalPrice;
    private double profit;

    public SalesReportRow(String date, String customerName, String customerEmail, String productName, int quantity, double unitPrice, double unitBuyingPrice, double totalPrice, double profit) {
        this.date = date;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitBuyingPrice = unitBuyingPrice;
        this.totalPrice = totalPrice;
        this.profit = profit;
    }

    public String getDate() { return date; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getUnitBuyingPrice() { return unitBuyingPrice; }
    public double getTotalPrice() { return totalPrice; }
    public double getProfit() { return profit; }

    @Override
    public String toString() {
        return "SalesReportRow{" +
                "date='" + date + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
