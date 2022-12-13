package com.ModbusConnector.service;

import com.ModbusConnector.model.TableReports;
import com.ModbusConnector.repository.TableReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ServiceReport {

    @Autowired
    private TableReportsRepository tableReportsRepository;

    public String addReport(float actForce1R, float actForce2R, int authorId, float maxDeformation, int actNum, int drawNum,
                            float ostDeformation, float needForce, int resultR, int numberProd) {
        Date date = new Date();
        System.out.println("test " + authorId);
        System.out.println("test " + actForce1R);
        tableReportsRepository.addReport(actForce1R, actForce2R, authorId, date, maxDeformation, actNum, drawNum, ostDeformation, needForce, resultR, numberProd);
        return "ok";
    }

    public List<TableReports> infoReports() {
        return tableReportsRepository.stendInfo();
    }

}
