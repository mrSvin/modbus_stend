package com.ModbusConnector;

import com.mysql.cj.jdbc.Driver;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

}
