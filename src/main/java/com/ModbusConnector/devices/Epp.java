package com.ModbusConnector.devices;

import com.ModbusConnector.MySQL;
import com.ModbusConnector.Solution;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class Epp {

    List<List> parserData = new ArrayList<>();

    private String dayNow = "null";
    private String dayOld = "null";

    private static int triger_work = 0;
    private static int triger_pause = 0;
    private static int triger_off = 0;
    private static int triger_avar = 0;
    private static int triger_nagruzka = 0;

    private static int procent_work = 0;
    private static int procent_pause = 0;
    private static int procent_off = 0;
    private static int procent_avar = 0;
    private static int procent_nagruzka = 0;

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
    private int countConnect = 0;
    private String complexTable = "epp_days";

    @Autowired
    Solution solution;
    @Autowired
    MySQL mySQL;

    public void data() {

        try {
            modbusClient = new ModbusClient("192.168.17.92", 777);

            if (connect(modbusClient)) {
                parser(modbusClient);
                status = findStatus(parserData);
                countConnect = 0;
            } else {
                countConnect++;
                if (countConnect > 3) {
                    status = 3;
                }

            }
            createTableSQL(complexTable);
            writeZagruzkaSQL(complexTable, status);
            writeRabotaArray(status);
            writeRabotaSQL(complexTable);
//            System.out.println(solution.dateTimeNow() + " " + getClass().getSimpleName() + " данные записаны");
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

            List<Integer> intValues = new ArrayList<>();
            List<Float> floatValues = new ArrayList<>();


            parserData.clear();
            parserData.add(intValues);
            parserData.add(floatValues);


            int[] dataInt;
            try {
                dataInt = modbusClient.ReadHoldingRegisters(0, 1);
                intValues.add(dataInt[0]);

                //System.out.println("tok: " + intValues.get(0));

            } catch (ModbusException | IOException e) {
//                    e.printStackTrace();
                System.out.println("Ошибка при чтении данных");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //status_work: 1 -работа, 2- пауза, 3-выключен, 4- авария 5-нагрузка
    private int findStatus(List<List> parserData) {
        int tok = (int) parserData.get(0).get(0);

        if (tok > 0) {
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
                con = mySQL.mysqlConnect(con);
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
        try {
            con = mySQL.mysqlConnect(con);
            stmt = con.createStatement();
            String tableName = solution.dateNow();

            //sost_rabota: 1 -работа, 2- пауза, 3-выключен, 4- авария
            if (status == 1) {
                procent_work++;
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = '" + procent_work + "' WHERE (`id` = '1')";
                stmt.executeUpdate(sql_request);
            }
            if (status == 2) {
                procent_pause++;
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = '" + procent_pause + "' WHERE (`id` = '2')";
                stmt.executeUpdate(sql_request);
            }
            if (status == 3) {
                procent_off++;
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = '" + procent_off + "' WHERE (`id` = '3')";
                stmt.executeUpdate(sql_request);
            }
            if (status == 4) {
                procent_avar++;
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = '" + procent_avar + "' WHERE (`id` = '4')";
                stmt.executeUpdate(sql_request);
            }
            if (status == 1) {
                procent_nagruzka++;
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `zagruzka` = '" + procent_nagruzka + "' WHERE (`id` = '5')";
                stmt.executeUpdate(sql_request);
            }

            con.close();

        } catch (SQLException e) {
            e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
            System.out.println("Ошибка SQL !");
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

        try {
            String tableName = solution.dateNow();

            con = mySQL.mysqlConnect(con);
            stmt = con.createStatement();
            sql_request = "SELECT COUNT(*) FROM " + schemaName + ".`" + tableName + "`";
            ResultSet rs = stmt.executeQuery(sql_request);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("COUNT(*)");
            }

            //Записываем тригеры работы
            int length_array_work = work_arrayList.size();
            if (length_array_work > 0 && length_array_work < count) {
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_work` = '" + work_arrayList.get(length_array_work - 1) + "' WHERE (`id` = '" + length_array_work + "');";
                stmt.executeUpdate(sql_request);
            }
            //Записываем тригеры паузы
            int length_array_pause = pause_arrayList.size();
            if (length_array_pause > 0 && length_array_pause < count) {
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_pause` = '" + pause_arrayList.get(length_array_pause - 1) + "' WHERE (`id` = '" + length_array_pause + "');";
                stmt.executeUpdate(sql_request);
            }
            //Записываем тригеры выключения
            int length_array_off = off_arrayList.size();
            if (length_array_off > 0 && length_array_off < count) {

                String sql_rabota = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_off` = '" + off_arrayList.get(length_array_off - 1) + "' WHERE (`id` = '" + length_array_off + "');";
                stmt.executeUpdate(sql_rabota);

            }
            //Записываем тригеры аварии
            int length_array_avar = avar_arrayList.size();
            if (length_array_avar > 0 && length_array_avar < count) {

                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_avar` = '" + avar_arrayList.get(length_array_avar - 1) + "' WHERE (`id` = '" + length_array_avar + "');";
                stmt.executeUpdate(sql_request);

            }
            //Записываем тригеры нагрузки
            int length_array_nagruzka = nagruzka_arrayList.size();
            if (length_array_nagruzka > 0 && length_array_nagruzka < count) {
                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_nagruzka` = '" + nagruzka_arrayList.get(length_array_nagruzka - 1) + "' WHERE (`id` = '" + length_array_nagruzka + "');";
                stmt.executeUpdate(sql_request);

            }
            //Записываем тригеры имени
            int length_array_name = programname_arrayList.size();
            if (length_array_name > 0 && length_array_name < count) {

                sql_request = "UPDATE `" + schemaName + "`.`" + tableName + "` SET `triger_name` = '" + programname_arrayList.get(length_array_name - 1) + "' WHERE (`id` = '" + length_array_name + "');";
                stmt.executeUpdate(sql_request);

            }


        } catch (SQLException e) {
            //e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
            //System.out.println("Ошибка SQL !");
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }


}
