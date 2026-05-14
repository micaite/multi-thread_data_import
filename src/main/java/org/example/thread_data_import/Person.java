package org.example.thread_data_import;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Person {

    private final StringProperty id           = new SimpleStringProperty();
    private final StringProperty firstName    = new SimpleStringProperty();
    private final StringProperty lastName     = new SimpleStringProperty();
    private final StringProperty email        = new SimpleStringProperty();
    private final StringProperty gender       = new SimpleStringProperty();
    private final StringProperty country      = new SimpleStringProperty();
    private final StringProperty domainName   = new SimpleStringProperty();
    private final StringProperty birthDate    = new SimpleStringProperty();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Person(String id, String firstName, String lastName, String email,
                  String gender, String country, String domainName, String birthDate) {
        this.id.set(id);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.email.set(email);
        this.gender.set(gender);
        this.country.set(country);
        this.domainName.set(domainName);
        this.birthDate.set(birthDate);
    }

    public LocalDate getBirthDateParsed() {
        try {
            return LocalDate.parse(birthDate.get(), FMT);
        } catch (Exception e) {
            return null;
        }
    }

    public int getIdInt() {
        try {
            return Integer.parseInt(id.get().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public StringProperty idProperty()         { return id; }
    public StringProperty firstNameProperty()  { return firstName; }
    public StringProperty lastNameProperty()   { return lastName; }
    public StringProperty emailProperty()      { return email; }
    public StringProperty genderProperty()     { return gender; }
    public StringProperty countryProperty()    { return country; }
    public StringProperty domainNameProperty() { return domainName; }
    public StringProperty birthDateProperty()  { return birthDate; }


    public String getId()         { return id.get(); }
    public String getFirstName()  { return firstName.get(); }
    public String getLastName()   { return lastName.get(); }
    public String getEmail()      { return email.get(); }
    public String getGender()     { return gender.get(); }
    public String getCountry()    { return country.get(); }
    public String getDomainName() { return domainName.get(); }
    public String getBirthDate()  { return birthDate.get(); }
}