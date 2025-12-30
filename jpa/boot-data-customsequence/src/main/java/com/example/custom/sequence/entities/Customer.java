package com.example.custom.sequence.entities;

import com.example.custom.sequence.config.db.StringPrefixedSequence;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @StringPrefixedSequence(valuePrefix = "CUS", numberFormat = "%05d")
    private String id;

    @Column(nullable = false)
    private String text;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public Customer() {}

    public String getId() {
        return id;
    }

    public Customer setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public Customer setText(String text) {
        this.text = text;
        return this;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Customer setOrders(List<Order> orders) {
        this.orders = orders;
        return this;
    }

    public Customer addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
        return this;
    }

    public Customer removeOrder(Order removedOrder) {
        orders.remove(removedOrder);
        removedOrder.setCustomer(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Customer customer = (Customer) o;
        return id != null && Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
