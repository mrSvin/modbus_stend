package com.ModbusConnector;

import com.mysql.cj.jdbc.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {


    public static Connection mysqlConnect(Connection con) {
        try {
            DriverManager.registerDriver(new Driver());
            String mysqlUrl = "jdbc:mysql://localhost/?useUnicode=true&serverTimezone=UTC&useSSL=false";
            con = DriverManager.getConnection(mysqlUrl, "root", "root");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return con;
    }
}
