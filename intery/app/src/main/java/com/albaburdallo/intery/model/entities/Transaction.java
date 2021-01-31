package com.albaburdallo.intery.model.entities;

import java.util.Date;

public class Transaction extends Base {

    private Boolean isExpenditure;
    private Boolean isIncome;
    private String concept;
    private Double money;
    private Date date;
    private String notes;
    //Entidad
    private long entityId;
    //Sección
    private long sectionId;

    public Transaction(Boolean isExpenditure, Boolean isIncome, String concept, Double money, Date date, String notes) {
        super();
        this.isExpenditure = isExpenditure;
        this.isIncome = isIncome;
        this.concept = concept;
        this.money = money;
        this.date = date;
        this.notes = notes;
    }

    public Boolean getExpenditure() {
        return isExpenditure;
    }

    public void setExpenditure(Boolean expenditure) {
        isExpenditure = expenditure;
    }

    public Boolean getIncome() {
        return isIncome;
    }

    public void setIncome(Boolean income) {
        isIncome = income;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
