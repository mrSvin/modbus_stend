package com.ModbusConnector.api.response;

import lombok.Data;

import java.util.Date;

@Data
public class ResponseStend {
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
}
