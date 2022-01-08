package com.example.archunit.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.google.common.base.Objects;

@MappedSuperclass
public abstract class Base implements Serializable {

	private static final long serialVersionUID = -2053886894431223968L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(nullable = false)
	private Integer version;

	@Column(nullable = false, columnDefinition = "boolean default true")
	private Boolean active = Boolean.TRUE;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, active, version);
	}

	@Override
	public boolean equals(Object obj) {
		return Objects.equal(this, obj);
	}
}
