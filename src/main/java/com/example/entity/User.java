package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kardash on 07.06.2016.
 */
@Entity
public class User {
    @Id
    @Column(updatable = false)
    private UUID uuid = UUID.randomUUID();
    private String firstName;
    private String lastName;
    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<Receipt> receipts;
    private String password;
    private String email;

    public User() {

    }

    public User(String firstName, String lastName, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
    }


    public void addReceipt(Receipt receipt) {
        this.receipts.add(receipt);
        if (receipt.getOwner() != this) {
            receipt.setOwner(this);
        }
    }

    public boolean allBasicFieldsNotEmpty() {
        if ((this.firstName == null) || (this.lastName == null) || (this.email == null) || (this.password == null)) {
            return false;
        } else if ((this.firstName.equals("")) || (this.lastName.equals("")) || (this.email.equals("")) || (this.password.equals("")))
            return false;
        else return true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }
}
