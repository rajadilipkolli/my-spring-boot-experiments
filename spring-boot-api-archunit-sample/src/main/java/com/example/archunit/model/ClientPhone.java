package com.example.archunit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "clientPhone")
public class ClientPhone extends Base {

    private static final long serialVersionUID = 1396542167093193958L;

    @Column(nullable = false, length = 80)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    private PhoneType phoneType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getId(), this.getActive(), this.getVersion(), number, phoneType, client);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
