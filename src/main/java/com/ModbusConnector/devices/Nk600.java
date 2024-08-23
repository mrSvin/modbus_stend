package com.ModbusConnector.devices;

import com.ModbusConnector.MySQL;
import com.ModbusConnector.Solution;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Nk600 {

    List<List> parserData = new ArrayList<>();

    private String dayNow = "null";
    private String dayOld = "null";

    private int triger_work = 0;
    private int triger_pause = 0;
    private int triger_off = 0;
    private int triger_avar = 0;
    private int triger_nagruzka = 0;

    private int procent_work = 0;
    private int procent_pause = 0;
    private int procent_off = 0;
    private int procent_avar = 0;
    private int procent_nagruzka = 0;

    private ArrayList<String> work_arrayList = new ArrayList<String>();
    private ArrayList<String> pause_arrayList = new ArrayList<String>();
    private ArrayList<String> off_arrayList = new ArrayList<String>();
    private ArrayList<String> avar_arrayList = new ArrayList<String>();
    private ArrayList<String> nagruzka_arrayList = new ArrayList<String>();
    private ArrayList<String> programname_arrayList = new ArrayList<String>();

    private Connection con;
    private Statement stmt;
    private String sql_request;
    private int status = 3;
    private ModbusClient modbusClient;
    private int countConnect=0;
    private String complexTable = "nk600_days";

    @Autowired
    Solution solution;
    @Autowired
    MySQL mySQL;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(1, "1");
        STATUS_MAP.put(2, "2");
        STATUS_MAP.put(3, "3");
        STATUS_MAP.put(4, "4");
    }

    public void data() {

        try {
            modbusClient = new ModbusClient("192.168.8.121", 777);

            if (connect(modbusClient)) {
                parser(modbusClient);
                status = findStatus(parserData);
                countConnect = 0;
            } else {
                countConnect++;
                if (countConnect>3) {
                    status = 3;
                }

            }
            createTableSQL(complexTable);
            writeZagruzkaSQL(complexTable, status);
            writeRabotaArray(status);
            writeRabotaSQL(complexTable);
        } finally {
            try {
                modbusClient.Disconnect();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }

    public boolean connect(ModbusClient target) {
        try {
            target.Connect();
            target.setConnectionTimeout(3000);
            //System.out.println("соединение установлено");
            return true;
        } catch (IOException e) {
            System.out.println("соединение не установлено " + getClass().getName());
            //e.printStackTrace();

            return false;
        }
    }

    public void parser(ModbusClient modbusClient) {

        try {

            Thread thread = new Thread(() -> {

                List<Integer> intValues = new ArrayList<>();
                List<Float> floatValues = new ArrayList<>();


                parserData.clear();
                parserData.add(intValues);
                parserData.add(floatValues);


                int[] dataInt;
                try {
                    dataInt = modbusClient.ReadHoldingRegisters(0, 1);
                    intValues.add(dataInt[0]);

                    //System.out.println("oborots: " + intValues.get(0));

                } catch (ModbusException | IOException e) {
//                    e.printStackTrace();
                    System.out.println("Ошибка при чтении данных");
                }
            });
            thread.start();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            thread.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //status_work: 1 -работа, 2- пауза, 3-выключен, 4- авария 5-нагрузка
    private int findStatus(List<List> parserData) {
        int oborots = (int) parserData.get(0).get(0);

        if (oborots > 3) {
            status = 1;
        } else {
            status = 2;
        }
        //System.out.println("findStatus: " + status);
        return status;
    }

    private void createTableSQL(String schemaName) {

        dayNow = solution.dateNow();

        if (dayNow.equals(dayOld) == false) {

            mySQL.deleteTableSQL(complexTable);

            procent_work = 0;
            procent_pause = 0;
            procent_off = 0;
            procent_avar = 0;
            procent_nagruzka = 0;

            triger_work = 0;
            triger_pause = 0;
            triger_off = 0;
            triger_avar = 0;
            triger_nagruzka = 0;


            work_arrayList = new ArrayList<String>();
            pause_arrayList = new ArrayList<String>();
            off_arrayList = new ArrayList<String>();
            avar_arrayList = new ArrayList<String>();
            nagruzka_arrayList = new ArrayList<String>();


            try {
                Connection con = mySQL.mysqlConnect();
                stmt = con.createStatement();
                String tableName = solution.dateNow();
                sql_request = "CREATE TABLE `" + schemaName + "`.`" + tableName + "` (`id` INT NOT NULL AUTO_INCREMENT,`zagruzka` INT,`triger_work` VARCHAR(45),`triger_pause` VARCHAR(45),`triger_off` VARCHAR(45),`triger_avar` VARCHAR(45), `triger_nagruzka` VARCHAR(45),`triger_name` VARCHAR(45),PRIMARY KEY (`id`));";
                stmt.executeUpdate(sql_request);

                //Заполняем нулями
                for (int i = 1; i <= 400; i++) {
                    sql_request = "INSERT INTO `" + schemaName + "`.`" + tableName + "` (`zagruzka`) VALUES ('0'); ";
                    stmt.executeUpdate(sql_request);
                }

                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_work` = '" + tableName + " 23:59:59" + "' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }
                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_pause` = '" + tableName + " 23:59:59" + "' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }
                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_off` = '" + tableName + " 23:59:59" + "' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }
                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_avar` = '" + tableName + " 23:59:59" + "' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }
                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_nagruzka` = '" + tableName + " 23:59:59" + "' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }
                for (int i = 1; i <= 400; i++) {
                    sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_name` = 'null' WHERE (`id` = '" + i + "')";
                    stmt.executeUpdate(sql_request);
                }

                con.close();

            } catch (SQLException e) {
                //e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
                System.out.println("Ошибка создания таблицы " + schemaName + ", вероятно таблица уже создана");
            }
            dayOld = dayNow;
        }
    }

    private void writeZagruzkaSQL(String schemaName, int status) {

        String tableName = solution.dateNow();
        String sql = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = ? WHERE `id` = ?";
        String id = STATUS_MAP.get(status);

        try (Connection con = mySQL.mysqlConnect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            switch (status) {
                case 1:
                    procent_work++;
                    pstmt.setInt(1, procent_work);
                    break;
                case 2:
                    procent_pause++;
                    pstmt.setInt(1, procent_pause);
                    break;
                case 3:
                    procent_off++;
                    pstmt.setInt(1, procent_off);
                    break;
                case 4:
                    procent_avar++;
                    pstmt.setInt(1, procent_avar);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected status: " + status);
            }

            pstmt.setString(2, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            // Используйте логирование вместо printStackTrace
            System.err.println("Ошибка SQL: " + e.getMessage());
        }

    }

    private void writeRabotaArray(int status) {

        String timeDateNow = solution.dateTimeNow();
        //Фиксируем работу
        if (status == 1 && triger_work != 1) {
            triger_work = 1;
            work_arrayList.add(timeDateNow);
            programname_arrayList.add("null");
        }
        if (status != 1 && triger_work != 0) {
            triger_work = 0;
            work_arrayList.add(timeDateNow);
        }
        //Фиксируем ожидание
        if (status == 2 && triger_pause != 1) {
            triger_pause = 1;
            pause_arrayList.add(timeDateNow);
        }
        if (status != 2 && triger_pause != 0) {
            triger_pause = 0;
            pause_arrayList.add(timeDateNow);
        }
        //Фиксируем выключение
        if (status == 3 && triger_off != 1) {
            triger_off = 1;
            off_arrayList.add(timeDateNow);
        }
        if (status != 3 && triger_off != 0) {
            triger_off = 0;
            off_arrayList.add(timeDateNow);
        }
        //Фиксируем аварию
        if (status == 4 && triger_avar != 1) {
            triger_avar = 1;
            avar_arrayList.add(timeDateNow);
        }
        if (status != 4 && triger_avar != 0) {
            triger_avar = 0;
            avar_arrayList.add(timeDateNow);
        }

        //Фиксируем нагрузку
        if (status == 1 && triger_nagruzka != 1) {
            triger_nagruzka = 1;
            nagruzka_arrayList.add(timeDateNow);
        }
        if (status != 1 && triger_nagruzka != 0) {
            triger_nagruzka = 0;
            nagruzka_arrayList.add(timeDateNow);
        }

    }

    private void writeRabotaSQL(String schemaName) {
        String tableName = solution.dateNow();

        String countQuery = "SELECT COUNT(*) AS count FROM " + schemaName + ".`" + tableName + "`";

        try (Connection con = mySQL.mysqlConnect();
             PreparedStatement countStmt = con.prepareStatement(countQuery);
             ResultSet rs = countStmt.executeQuery()) {

            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }

            mySQL.updateTrigger(con, schemaName, tableName, "triger_work", work_arrayList, count);
            mySQL.updateTrigger(con, schemaName, tableName, "triger_pause", pause_arrayList, count);
            mySQL.updateTrigger(con, schemaName, tableName, "triger_off", off_arrayList, count);
            mySQL.updateTrigger(con, schemaName, tableName, "triger_avar", avar_arrayList, count);
            mySQL.updateTrigger(con, schemaName, tableName, "triger_nagruzka", nagruzka_arrayList, count);
            mySQL.updateTrigger(con, schemaName, tableName, "triger_name", programname_arrayList, count);

        } catch (SQLException e) {
            // Используйте логирование вместо printStackTrace
            System.err.println("Ошибка SQL: " + e.getMessage());
        }
    }


}
