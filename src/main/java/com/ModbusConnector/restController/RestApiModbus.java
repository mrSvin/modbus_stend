package com.ModbusConnector.restController;


import com.ModbusConnector.devices.Stend;
import com.ModbusConnector.api.response.ResponseStend;
import com.ModbusConnector.model.TableReports;
import com.ModbusConnector.service.ServiceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://192.168.3.41:8080", "http://192.168.3.152:8080", "http://iot.sespel.com"}, maxAge = 3600)
@RestController
@RequestMapping("/api")
public class RestApiModbus {

    @Autowired
    private ServiceReport serviceReport;
    @Autowired
    private Stend stend;

    @GetMapping("/modbusData")
    private List<TableReports> modbusData() {
        return serviceReport.infoReports();
    }

    @GetMapping("/stendLastData")
    private ResponseStend stendLastData() {
        return stend.lastData();
    }




}
