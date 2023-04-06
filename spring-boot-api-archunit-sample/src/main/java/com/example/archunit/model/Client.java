package com.example.archunit.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "client")
public class Client extends Base {

    private static final long serialVersionUID = -2974615880078954663L;

    @Column(nullable = false, length = 100)
    private String firstname;

    @Column(nullable = false, length = 100)
    private String lastname;

    @Column(nullable = true, length = 100)
    private String documentType;

    @Column(nullable = true, length = 100)
    private String documentNumber;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "client",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<ClientPhone> phones;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "client",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<ClientEmail> emails;

    @Column(nullable = true, length = 100)
    private String observation;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<ClientPhone> getPhones() {
        return phones;
    }

    public void setPhones(List<ClientPhone> phones) {
        this.phones = phones;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public List<ClientEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<ClientEmail> emails) {
        this.emails = emails;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getId(),
                this.getActive(),
                this.getVersion(),
                firstname,
                lastname,
                documentType,
                documentNumber,
                phones,
                emails,
                observation);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
