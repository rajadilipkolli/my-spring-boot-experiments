package com.example.archunit.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

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
		return Objects.hashCode(this.getId(), 
				this.getActive(), 
				this.getVersion(),
				number,
				phoneType,
				client);
	}

	@Override
	public boolean equals(Object obj) {
		return Objects.equal(this, obj);
	}	
}
