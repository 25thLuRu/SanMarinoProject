package com.example.sanmarino;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Resident {

    private final StringProperty id       = new SimpleStringProperty();
    private final StringProperty last     = new SimpleStringProperty();
    private final StringProperty middle   = new SimpleStringProperty();
    private final StringProperty first    = new SimpleStringProperty();
    private final StringProperty contact  = new SimpleStringProperty();
    private final StringProperty email    = new SimpleStringProperty();
    private final StringProperty house    = new SimpleStringProperty();
    private final StringProperty street   = new SimpleStringProperty();
    private final StringProperty civil    = new SimpleStringProperty();
    private final StringProperty birth    = new SimpleStringProperty();

    public Resident(String id, String last, String middle, String first,
                    String contact, String email,
                    String house, String street, String civil, String birth) {

        this.id.set(id);
        this.last.set(last);
        this.middle.set(middle);
        this.first.set(first);
        this.contact.set(contact);
        this.email.set(email);
        this.house.set(house);
        this.street.set(street);
        this.civil.set(civil);
        this.birth.set(birth);
    }

    public StringProperty idProperty()      { return id; }
    public StringProperty lastProperty()    { return last; }
    public StringProperty middleProperty()  { return middle; }
    public StringProperty firstProperty()   { return first; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty emailProperty()   { return email; }
    public StringProperty houseProperty()   { return house; }
    public StringProperty streetProperty()  { return street; }
    public StringProperty civilProperty()   { return civil; }
    public StringProperty birthProperty()   { return birth; }

    public String getId()      { return id.get(); }
    public String getLast()    { return last.get(); }
    public String getMiddle()  { return middle.get(); }
    public String getFirst()   { return first.get(); }
    public String getContact() { return contact.get(); }
    public String getEmail()   { return email.get(); }
    public String getHouse()   { return house.get(); }
    public String getStreet()  { return street.get(); }
    public String getCivil()   { return civil.get(); }
    public String getBirth()   { return birth.get(); }

    public void setId(String v)      { id.set(v); }
    public void setLast(String v)    { last.set(v); }
    public void setMiddle(String v)  { middle.set(v); }
    public void setFirst(String v)   { first.set(v); }
    public void setContact(String v) { contact.set(v); }
    public void setEmail(String v)   { email.set(v); }
    public void setHouse(String v)   { house.set(v); }
    public void setStreet(String v)  { street.set(v); }
    public void setCivil(String v)   { civil.set(v); }
    public void setBirth(String v)   { birth.set(v); }
}