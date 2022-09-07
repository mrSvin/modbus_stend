package com.ModbusConnector.service;

import com.ModbusConnector.Stend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class Connector extends Thread {

    private Stend stend;

    public Connector(Stend stend) {
        this.stend = stend;
    }

    @Scheduled(fixedRate = 5000)
    public void connector() throws IOException {

        System.out.println("активные потоки: " + Thread.activeCount());
        stend.data();
    }


}
