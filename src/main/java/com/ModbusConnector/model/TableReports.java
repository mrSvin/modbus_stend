package com.ModbusConnector.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class TableReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "INT")
    private String numberAct;

    @Column(columnDefinition = "INT")
    private String numberProd;

    @Column(columnDefinition = "VARCHAR(45)")
    private String numberDrawing;

    @Column(columnDefinition = "FLOAT")
    private float requiredForce;

    @Column(columnDefinition = "FLOAT")
    private float actualForce;

    @Column(columnDefinition = "FLOAT")
    private float actualForce2;

    @Column(columnDefinition = "FLOAT")
    private float maxDeformation;

    @Column(columnDefinition = "FLOAT")
    private float ostDeformation;

    @Column(columnDefinition = "VARCHAR(45)")
    private String valid;

    @Column(columnDefinition = "INT")
    private String authorId;

    @Column(columnDefinition = "DATETIME")
    private String dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumberAct() {
        return numberAct;
    }

    public void setNumberAct(String numberAct) {
        this.numberAct = numberAct;
    }

    public String getNumberProd() {
        return numberProd;
    }

    public void setNumberProd(String numberProd) {
        this.numberProd = numberProd;
    }

    public String getNumberDrawing() {
        return numberDrawing;
    }

    public void setNumberDrawing(String numberDrawing) {
        this.numberDrawing = numberDrawing;
    }

    public float getRequiredForce() {
        return requiredForce;
    }

    public void setRequiredForce(float requiredForce) {
        this.requiredForce = requiredForce;
    }

    public float getActualForce() {
        return actualForce;
    }

    public void setActualForce(float actualForce) {
        this.actualForce = actualForce;
    }

    public float getActualForce2() {
        return actualForce2;
    }

    public void setActualForce2(float actualForce2) {
        this.actualForce2 = actualForce2;
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

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
