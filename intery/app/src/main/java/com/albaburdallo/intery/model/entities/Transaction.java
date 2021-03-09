package com.albaburdallo.intery.model.entities;

import java.util.Date;

public class Transaction extends Base {

    private String id;
    private Boolean isExpenditure;
    private Boolean isIncome;
    private String concept;
    private Double money;
    private Date date;
    private String notes;
    //Entidad
    private Entity entity;
    //Sección
    private Section section;

    public Transaction(){
    }

    public Transaction(String id, Boolean isExpenditure, Boolean isIncome, String concept, Double money, Date date, String notes, Entity entity, Section section) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.isExpenditure = isExpenditure;
        this.isIncome = isIncome;
        this.concept = concept;
        this.money = money;
        this.date = date;
        this.notes = notes;
        this.entity = entity;
        this.section = section;
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

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
