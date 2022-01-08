package com.example.archunit.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "clientEmail")
public class ClientEmail extends Base {

	private static final long serialVersionUID = 1396542167093193958L;

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
		return Objects.hashCode(this.getId(), 
				this.getActive(), 
				this.getVersion(),
				email,
				emailType,
				client);
	}

	@Override
	public boolean equals(Object obj) {
		return Objects.equal(this, obj);
	}	
}
