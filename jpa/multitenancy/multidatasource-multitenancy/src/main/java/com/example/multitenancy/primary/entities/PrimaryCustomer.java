package com.example.multitenancy.primary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Version
    @Column(name = "version")
    private Short version = 0;

    @Column(nullable = false)
    @TenantId
    private String tenant = "primary";

    public Long getId() {
        return id;
    }

    public PrimaryCustomer setId(Long id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public PrimaryCustomer setText(String text) {
        this.text = text;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public PrimaryCustomer setVersion(Short version) {
        this.version = version;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public PrimaryCustomer setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass =
                o instanceof HibernateProxy
                        ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                        : o.getClass();
        Class<?> thisEffectiveClass =
                this instanceof HibernateProxy
                        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                        : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PrimaryCustomer that = (PrimaryCustomer) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this)
                        .getHibernateLazyInitializer()
                        .getPersistentClass()
                        .hashCode()
                : getClass().hashCode();
    }
}
