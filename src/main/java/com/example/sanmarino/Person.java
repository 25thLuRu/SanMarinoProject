package com.example.sanmarino;

import javafx.beans.property.*;

import java.text.SimpleDateFormat;

public class Person {
    private final IntegerProperty personTransactID;
    private final IntegerProperty personFormID;
    private final DoubleProperty personTransactAmount;
    private final IntegerProperty personTransactType;


    public Person(Integer id, Integer formId, Double amount, Integer type) {
        this.personTransactID = new SimpleIntegerProperty(id);
        this.personFormID = new SimpleIntegerProperty(formId);
        this.personTransactAmount = new SimpleDoubleProperty(amount);
        this.personTransactType = new SimpleIntegerProperty(type);
    }

    // ID
    public Integer getPersonTransactID() { return personTransactID.get(); }
    public void setPersonTransactID(Integer id) { this.personTransactID.set(id); }
    public IntegerProperty personTransactIDProperty() { return personTransactID; }

    // Form ID
    public Integer getPersonFormID() { return personFormID.get(); }
    public void setPersonFormID(Integer formId) { this.personFormID.set(formId); }
    public IntegerProperty personFormIDProperty() { return personFormID; }

    // Amount
    public Double getPersonTransactAmount() { return personTransactAmount.get(); }
    public void setPersonTransactAmount(Double amount) { this.personTransactAmount.set(amount); }
    public DoubleProperty personTransactAmountProperty() { return personTransactAmount; }

    // Type
    public Integer getPersonTransactType() { return personTransactType.get(); }
    public void setPersonTransactType(Integer type) { this.personTransactType.set(type); }
    public IntegerProperty personTransactTypeProperty() { return personTransactType; }
}
