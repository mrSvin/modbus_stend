package com.ModbusConnector.devices;

import com.ModbusConnector.MySQL;
import com.ModbusConnector.Solution;
import com.ModbusConnector.api.response.ResponseStend;
import com.ModbusConnector.repository.TableReportsRepository;
import com.ModbusConnector.service.ServiceReport;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
public class Stend {

    @Autowired
    private TableReportsRepository tableReportsRepository;
    @Autowired
    MySQL mySQL;

    List<List> parserData = new ArrayList<>();

    private int requestWriteDB;
    private int prodNum;
    private int actNum;
    private int resultR;
    private int drawNum;
    private int authorId;
    private int workMode;
    private float maxDeformation;
    private float ostDeformation;
    private float actForce1R;
    private float actForce2R;
    private float needForce;
    private Date lastRequest;

    private final ServiceReport serviceReport;

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
    private String complexTable = "stend_resources_days";

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    @Autowired
    Solution solution;

    static {
        STATUS_MAP.put(1, "1");
        STATUS_MAP.put(2, "2");
        STATUS_MAP.put(3, "3");
        STATUS_MAP.put(4, "4");
    }

    public Stend(ServiceReport serviceReport) {
        this.serviceReport = serviceReport;
    }

    public void data() {

        try {
            modbusClient = new ModbusClient("192.168.17.115", 502);

            if (connect(modbusClient)) {
                parser(modbusClient);

                status = findStatus(workMode);

                if (requestWriteDB == 1) {
                    if (checkAuthorId(authorId)) {
                        serviceReport.addReport(actForce1R, actForce2R, authorId, maxDeformation, actNum, drawNum, ostDeformation, needForce, resultR, prodNum);
                        writeCompliteWriteToDB(modbusClient, 2);
                    } else {
                        writeCompliteWriteToDB(modbusClient, 4);
                    }
                }
                countConnect=0;
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
            target.setConnectionTimeout(2000);
//            byte slaveId = 10;
//            target.setUnitIdentifier(slaveId);
            System.out.println("соединение установлено");
            return true;
        } catch (IOException e) {
            System.out.println("соединение не установлено");
//            e.printStackTrace();

            return false;
        }
    }

    public void parser(ModbusClient modbusClient) {

        try {


                List<Integer> intValues = new ArrayList<>();
                List<Float> floatValues = new ArrayList<>();
                List<Date> timeValues = new ArrayList<>();

                parserData.clear();
                parserData.add(intValues);
                parserData.add(floatValues);
                parserData.add(timeValues);

                int[] data_0_4 = new int[0];
                try {
//                        modbusClient.WriteSingleRegister(1, 21345);
                    data_0_4 = modbusClient.ReadHoldingRegisters(0, 4);
                    intValues.add(data_0_4[1]);
                    intValues.add(data_0_4[2]);
                    intValues.add(data_0_4[3]);

//                    System.out.println("requestWriteDB: " + intValues.get(0));
//                    System.out.println("prodNum: " + intValues.get(1));
//                    System.out.println("actNum: " + intValues.get(2));

                    Float maxDeformation = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(4, 2));
                    floatValues.add(maxDeformation);
//                    System.out.println("maxDeformation: " + floatValues.get(0));

                    Float ostDeformation = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(6, 2));
                    floatValues.add(ostDeformation);
//                    System.out.println("ostDeformation: " + floatValues.get(1));

                    Float actForce1R = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(8, 2));
                    floatValues.add(actForce1R);
//                    System.out.println("actForce1R: " + floatValues.get(2));

                    Float actForce2R = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(12, 2));
                    floatValues.add(actForce2R);
//                    System.out.println("actForce2R: " + floatValues.get(3));

                    Float needForce = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(16, 2));
                    floatValues.add(needForce);
//                    System.out.println("needForce: " + floatValues.get(4));

                    int authorId = ModbusClient.ConvertRegistersToDouble(modbusClient.ReadHoldingRegisters(20, 2));
                    intValues.add(authorId);
//                    System.out.println("authorId: " + authorId);

                    int[] data_22_25 = modbusClient.ReadHoldingRegisters(22, 3);
                    intValues.add(data_22_25[0]);
                    intValues.add(data_22_25[1]);
                    intValues.add(data_22_25[2]);
//                    System.out.println("resultR: " + intValues.get(4));
//                    System.out.println("drawNum: " + intValues.get(5));
//                    System.out.println("workMode: " + intValues.get(6));
                    timeValues.add(new Date());

                    writeoutputData(parserData);

                } catch (ModbusException | IOException e) {
//                    e.printStackTrace();
                    System.out.println("Ошибка при чтении данных");
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //status_work: 1 -работа, 2- пауза, 3-выключен, 4- авария 5-нагрузка
    private int findStatus(Integer workMode) {
        int status = 0;

        if (workMode == 1) {
            status = 1;
        } else {
            status = 2;
        }
        System.out.println("findStatus: " + status);
        return status;
    }

    private void writeCompliteWriteToDB(ModbusClient modbusClient, int value) {

        Thread thread = new Thread() {
            public void run() {
                try {
                    modbusClient.WriteSingleRegister(1, value);

                } catch (ModbusException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void writeoutputData(List<List> data) {
        requestWriteDB = (int) data.get(0).get(0);
        prodNum = (int) data.get(0).get(1);
        actNum = (int) data.get(0).get(2);
        authorId = (int) data.get(0).get(3);
        resultR = (int) data.get(0).get(4);
        drawNum = (int) data.get(0).get(5);
        workMode = (int) data.get(0).get(6);
        maxDeformation = (float) data.get(1).get(0);
        ostDeformation = (float) data.get(1).get(1);
        actForce1R = (float) data.get(1).get(2);
        actForce2R = (float) data.get(1).get(3);
        needForce = (float) data.get(1).get(4);
        lastRequest = (Date) data.get(2).get(0);
    }

    private void createTableSQL(String schemaName) {

        dayNow = dateNow();

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
                String tableName = dateNow();
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

        String timeDateNow = dateTimeNow();
        //Фиксируем работу
        if (status == 1 && triger_work != 1) {
            triger_work = 1;

            work_arrayList.add(timeDateNow);
            programname_arrayList.add(String.valueOf(actNum));
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

    public ResponseStend lastData() {
        ResponseStend responseStend = new ResponseStend();
        try {
            responseStend.setRequestWriteDB(requestWriteDB);
            responseStend.setProdNum(prodNum);
            responseStend.setActNum(actNum);
            responseStend.setAuthorId(authorId);
            responseStend.setResultR(resultR);
            responseStend.setDrawNum(drawNum);
            responseStend.setWorkMode(workMode);
            responseStend.setMaxDeformation(maxDeformation);
            responseStend.setOstDeformation(ostDeformation);
            responseStend.setActForce1R(actForce1R);
            responseStend.setActForce2R(actForce2R);
            responseStend.setNeedForce(needForce);
            responseStend.setLastRequest(lastRequest);

//            postRequest(authCoocie);

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return responseStend;
    }

    private String dateNow() {
        long unixTime = System.currentTimeMillis() / 1000L + 10800; //Определяем текущее время
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
//        Записываем текущее время
        return dateFormat.format(date);

    }

    private String dateTimeNow() {
        long unixTime = System.currentTimeMillis() / 1000L + 10800; //Определяем текущее время
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
//        Записываем текущее время
        return dateFormat.format(date);

    }

    private Boolean checkAuthorId(int authorId) {
        try {

            Connection con = mySQL.mysqlConnect();
            stmt = con.createStatement();
            sql_request = "SELECT COUNT(*) FROM stanki_auth.operator_stand where author_id = '" + authorId + "'";
            ResultSet rs = stmt.executeQuery(sql_request);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("COUNT(*)");
            }
            if (count > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
