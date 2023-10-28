package com.example.archunit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "clientEmail")
public class ClientEmail extends Base {

    @Column(nullable = false, length = 80)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private EmailType emailType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        this.emailType = emailType;
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
                this.getId(), this.getActive(), this.getVersion(), email, emailType, client);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
