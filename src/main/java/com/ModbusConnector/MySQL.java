package com.ModbusConnector;

import com.mysql.cj.jdbc.Driver;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;

@Component
public class MySQL {

    @Autowired
    Solution solution;

    String mysqlUrl = "jdbc:mysql://localhost/?useUnicode=true&serverTimezone=UTC&useSSL=false";
    String login = "root";
    String password = "root";

    @SneakyThrows
    public Connection mysqlConnect() {

        DriverManager.registerDriver(new Driver());
        return DriverManager.getConnection(mysqlUrl, login, password);
    }

    public void deleteTableSQL(String tableName) {
        String sqlRequest = "DROP TABLE `" + tableName + "`.`" + solution.dateNow() + "`";

        try (Connection con = mysqlConnect();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(sqlRequest);
            System.out.println("Удалена таблица " + tableName + "." + solution.dateNow());

        } catch (SQLException e) {
            System.out.println(tableName + " Удаление не произведено! Вероятно таблица отсутствует");
        }
    }

    public void updateTrigger(Connection con, String schemaName, String tableName, String columnName, List<String> arrayList, int count) throws SQLException {
        if (arrayList.size() > 0 && arrayList.size() < count) {
            String updateQuery = String.format("UPDATE `" + schemaName + "`.`" + tableName + "` SET `%s` = ? WHERE `id` = ?", columnName);
            try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                updateStmt.setString(1, arrayList.get(arrayList.size() - 1));
                updateStmt.setInt(2, arrayList.size());
                updateStmt.executeUpdate();
            }
        }
    }

}
