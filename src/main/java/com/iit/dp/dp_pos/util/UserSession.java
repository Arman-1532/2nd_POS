package com.iit.dp.dp_pos.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.iit.dp.dp_pos.controller.AuthController;

public class UserSession {
    public static boolean isAdmin() {
        if (AuthController.loggedInUserId == -1) return false;

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT type FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, AuthController.loggedInUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "admin".equalsIgnoreCase(rs.getString("type"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getCurrentUserEmail() {
        return ActivityLogger.getEmailByUserId(AuthController.loggedInUserId);
    }
}
