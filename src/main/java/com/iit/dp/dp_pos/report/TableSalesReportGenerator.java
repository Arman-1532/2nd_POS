package com.iit.dp.dp_pos.report;

import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TableSalesReportGenerator implements SalesReportGenerator {
    @Override
    public List<SalesReportRow> generateReport(LocalDate start, LocalDate end) {
        List<SalesReportRow> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String profitExpr = hasColumn(conn, "products", "buying_price")
                    ? "(oi.total_price - (oi.quantity * p.buying_price))"
                    : "(oi.total_price - 0)"; // fallback if no buying_price

            String sql = "SELECT date(o.date) as date, c.name as customerName, c.email as customerEmail, p.name as productName, oi.quantity, " +
                    "oi.unit_price, p.buying_price as unitBuyingPrice, oi.total_price, " +
                    profitExpr + " as profit " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id ";
            if (start != null && end != null) {
                sql += "WHERE date(o.date) BETWEEN ? AND ? ";
            }
            sql += "ORDER BY o.date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (start != null && end != null) {
                    stmt.setString(1, start.toString());
                    stmt.setString(2, end.toString());
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    rows.add(new SalesReportRow(
                            rs.getString("date"),
                            rs.getString("customerName"),
                            rs.getString("customerEmail"),
                            rs.getString("productName"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("unitBuyingPrice"),
                            rs.getDouble("total_price"),
                            rs.getDouble("profit")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    @Override
    public boolean export(List<SalesReportRow> rows, LocalDate start, LocalDate end) {
        // This class only generates data, not exports - method required by interface
        return false;
    }

    @Override
    public String getFormatName() {
        return "TABLE";
    }

    private boolean hasColumn(Connection conn, String table, String column) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, table, column)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
