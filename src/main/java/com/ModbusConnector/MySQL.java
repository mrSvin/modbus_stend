package com.ModbusConnector;

import com.mysql.cj.jdbc.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class MySQL {

    private Connection con;
    private Statement stmt;

    @Autowired
    Solution solution;

    public Connection mysqlConnect(Connection con) {
        try {
            DriverManager.registerDriver(new Driver());
            String mysqlUrl = "jdbc:mysql://localhost/?useUnicode=true&serverTimezone=UTC&useSSL=false";
            con = DriverManager.getConnection(mysqlUrl, "root", "root");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return con;
    }

    public void deleteTableSQL(String tableName) {

        try {
            con = mysqlConnect(con);
            stmt = con.createStatement();

            String sql_request = "DROP TABLE `" + tableName + "`.`" + solution.dateNow() + "`";
            stmt.executeUpdate(sql_request);
            System.out.println("Удалена таблица " + tableName + "." + solution.dateNow());

        } catch (SQLException e) {
//                e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
            System.out.println(tableName + " Удаление не произведено! Вероятно таблица отсутствует");
        } finally {
            try {
                con.close();
                stmt.close();
            } catch (SQLException throwables) {
                //throwables.printStackTrace();
            }
        }


    }

}
