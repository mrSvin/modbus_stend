package com.ModbusConnector;

import com.ModbusConnector.api.response.ResponseLastData;
import com.ModbusConnector.repository.TableReportsRepository;
import com.ModbusConnector.service.ServiceReport;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class Stend {

    @Autowired
    private TableReportsRepository tableReportsRepository;

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

    private static ArrayList<String> work_arrayList = new ArrayList<String>();
    private static ArrayList<String> pause_arrayList = new ArrayList<String>();
    private static ArrayList<String> off_arrayList = new ArrayList<String>();
    private static ArrayList<String> avar_arrayList = new ArrayList<String>();
    private static ArrayList<String> nagruzka_arrayList = new ArrayList<String>();
    private static ArrayList<String> programname_arrayList = new ArrayList<String>();

    private Connection con;
    private Statement stmt;
    private String sql_request;

    public Stend(ServiceReport serviceReport) {
        this.serviceReport = serviceReport;
    }

    public void data() throws IOException {

        ModbusClient modbusClient = new ModbusClient("192.168.17.115", 502);

        if (connect(modbusClient)) {
            parser(modbusClient);

            if (parserData.get(0).size() > 0) {

                int status = findStatus(parserData);
                create_table_SQL("stend_resources_days");

                List<Integer> intData = parserData.get(0);
                if (intData.get(0) == 1) {
                    serviceReport.addReport(actForce1R, authorId, maxDeformation, actNum, drawNum, ostDeformation, needForce, resultR);
                    writeCompliteWriteToDB(modbusClient);
                }

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

            Thread thread = new Thread() {
                public void run() {

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

                        System.out.println("requestWriteDB: " + intValues.get(0));
                        System.out.println("prodNum: " + intValues.get(1));
                        System.out.println("actNum: " + intValues.get(2));

                        Float maxDeformation = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(4, 2));
                        floatValues.add(maxDeformation);
                        System.out.println("maxDeformation: " + floatValues.get(0));

                        Float ostDeformation = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(6, 2));
                        floatValues.add(ostDeformation);
                        System.out.println("ostDeformation: " + floatValues.get(1));

                        Float actForce1R = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(8, 2));
                        floatValues.add(actForce1R);
                        System.out.println("actForce1R: " + floatValues.get(2));

                        Float actForce2R = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(12, 2));
                        floatValues.add(actForce2R);
                        System.out.println("actForce2R: " + floatValues.get(3));

                        Float needForce = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(16, 2));
                        floatValues.add(needForce);
                        System.out.println("needForce: " + floatValues.get(4));

                        int authorId = ModbusClient.ConvertRegistersToDouble(modbusClient.ReadHoldingRegisters(20, 2));
                        System.out.println("authorId: " + authorId);
                        intValues.add(authorId);

                        int[] data_22_25 = modbusClient.ReadHoldingRegisters(22, 3);
                        intValues.add(data_22_25[0]);
                        intValues.add(data_22_25[1]);
                        intValues.add(data_22_25[2]);
                        System.out.println("resultR: " + intValues.get(3));
                        System.out.println("drawNum: " + intValues.get(4));
                        System.out.println("workMode: " + intValues.get(5));
                        timeValues.add(new Date());

                        writeoutputData(parserData);

                    } catch (ModbusException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    //status_work: 1 -работа, 2- пауза, 3-выключен, 4- авария 5-нагрузка
    private int findStatus(List<List> data) {
        int status = 0;
        List<Integer> dataInts = data.get(0);
        if (dataInts.get(5) == 1) {
            status = 1;
        } else {
            status = 2;
        }
        System.out.println("findStatus: " + status);
        return status;
    }

    private void writeCompliteWriteToDB(ModbusClient modbusClient) {

        Thread thread = new Thread() {
            public void run() {
                try {
                    modbusClient.WriteSingleRegister(1, 2);

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
        resultR = (int) data.get(0).get(3);
        drawNum = (int) data.get(0).get(4);
        authorId = (int) data.get(0).get(5);
        workMode = (int) data.get(0).get(6);
        maxDeformation = (float) data.get(1).get(0);
        ostDeformation = (float) data.get(1).get(1);
        actForce1R = (float) data.get(1).get(2);
        actForce2R = (float) data.get(1).get(3);
        needForce = (float) data.get(1).get(4);
        lastRequest = (Date) data.get(2).get(0);
    }

    private void create_table_SQL(String schemaName) {

        dayNow = dateNow();

        if (dayNow.equals(dayOld) == false) {

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
                con = MySQL.mysqlConnect(con);
                stmt = con.createStatement();
                String tableName = dateNow();
                sql_request = "CREATE TABLE `" + schemaName + "`.`" + tableName + "` (`id` INT NOT NULL AUTO_INCREMENT,`zagruzka` INT,`triger_work` VARCHAR(45),`triger_pause` VARCHAR(45),`triger_off` VARCHAR(45),`triger_avar` VARCHAR(45), `triger_nagruzka` VARCHAR(45),`triger_name` VARCHAR(45),PRIMARY KEY (`id`));";
                stmt.executeUpdate(sql_request);
                con.close();

            } catch (SQLException e) {
                e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
                System.out.println("Ошибка SQL !");
            }
            dayOld = dayNow;
        }
    }

    public ResponseLastData lastData() {
        ResponseLastData responseLastData = new ResponseLastData();
        try {
            responseLastData.setRequestWriteDB(requestWriteDB);
            responseLastData.setProdNum(prodNum);
            responseLastData.setActNum(actNum);
            responseLastData.setAuthorId(resultR);
            responseLastData.setResultR(drawNum);
            responseLastData.setDrawNum(authorId);
            responseLastData.setWorkMode(workMode);
            responseLastData.setMaxDeformation(maxDeformation);
            responseLastData.setOstDeformation(ostDeformation);
            responseLastData.setActForce1R(actForce1R);
            responseLastData.setActForce2R(actForce2R);
            responseLastData.setNeedForce(needForce);
            responseLastData.setLastRequest(lastRequest);

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return responseLastData;
    }

    private String dateNow() {
        long unixTime = System.currentTimeMillis() / 1000L + 10800; //Определяем текущее время
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
//        Записываем текущее время
        return dateFormat.format(date);

    }

//    private List<String> apiModbusInfo() {
//        List<String> resultData = new ArrayList<>();
//        ModbusClient modbusClient = new ModbusClient("192.168.17.115", 502);
//        if (connect(modbusClient)) {
//            List<List> data = parser(modbusClient);
//            if (data.get(0).size() > 0) {
//
//                for (int i = 0; i < data.get(0).size(); i++) {
//                    resultData.add(String.valueOf(data.get(0).get(i)));
//                }
//                for (int i = 0; i < data.get(1).size(); i++) {
//                    resultData.add(String.valueOf(data.get(1).get(i)));
//                }
//                return resultData;
//            } else {
//                return resultData;
//            }
//
//        }
//        return resultData;
//    }

}
