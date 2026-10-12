package com.example.custom.sequence.entities;

import com.example.custom.sequence.config.db.StringPrefixedSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @StringPrefixedSequence(sequenceName = "orders_seq", numberFormat = "%09d")
    private String id;

    @Column(nullable = false)
    private String itemName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Order() {}

    public String getId() {
        return id;
    }

    public Order setId(String id) {
        this.id = id;
        return this;
    }

    public String getItemName() {
        return itemName;
    }

    public Order setItemName(String text) {
        this.itemName = text;
        return this;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Order setCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Order order = (Order) o;
        return Objects.equals(itemName, order.itemName) && Objects.equals(customer.getId(), order.customer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
