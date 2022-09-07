package com.ModbusConnector.api.response;

import java.util.Date;

public class ResponseLastData {
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

    public int getRequestWriteDB() {
        return requestWriteDB;
    }

    public void setRequestWriteDB(int requestWriteDB) {
        this.requestWriteDB = requestWriteDB;
    }

    public int getProdNum() {
        return prodNum;
    }

    public void setProdNum(int prodNum) {
        this.prodNum = prodNum;
    }

    public int getActNum() {
        return actNum;
    }

    public void setActNum(int actNum) {
        this.actNum = actNum;
    }

    public int getResultR() {
        return resultR;
    }

    public void setResultR(int resultR) {
        this.resultR = resultR;
    }

    public int getDrawNum() {
        return drawNum;
    }

    public void setDrawNum(int drawNum) {
        this.drawNum = drawNum;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public float getMaxDeformation() {
        return maxDeformation;
    }

    public void setMaxDeformation(float maxDeformation) {
        this.maxDeformation = maxDeformation;
    }

    public float getOstDeformation() {
        return ostDeformation;
    }

    public void setOstDeformation(float ostDeformation) {
        this.ostDeformation = ostDeformation;
    }

    public float getActForce1R() {
        return actForce1R;
    }

    public void setActForce1R(float actForce1R) {
        this.actForce1R = actForce1R;
    }

    public float getActForce2R() {
        return actForce2R;
    }

    public void setActForce2R(float actForce2R) {
        this.actForce2R = actForce2R;
    }

    public float getNeedForce() {
        return needForce;
    }

    public void setNeedForce(float needForce) {
        this.needForce = needForce;
    }

    public Date getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(Date lastRequest) {
        this.lastRequest = lastRequest;
    }
}
