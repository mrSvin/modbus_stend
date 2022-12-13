package com.ModbusConnector;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Solution {

    public String dateNow() {
        long unixTime = System.currentTimeMillis() / 1000L + 10800; //Определяем текущее время
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
//        Записываем текущее время
        return dateFormat.format(date);

    }

    public String dateTimeNow() {
        long unixTime = System.currentTimeMillis() / 1000L + 10800; //Определяем текущее время
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
//        Записываем текущее время
        return dateFormat.format(date);

    }

}
