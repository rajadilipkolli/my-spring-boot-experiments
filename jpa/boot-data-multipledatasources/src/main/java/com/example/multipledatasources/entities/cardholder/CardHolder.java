package com.example.multipledatasources.entities.cardholder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.StringJoiner;
import org.hibernate.Hibernate;

@Entity
@Table(name = "card_holder")
public class CardHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "member_id")
    private String memberId;

    @Column(nullable = false, name = "card_number")
    private String cardNumber;

    public Long getId() {
        return id;
    }

    public CardHolder setId(Long id) {
        this.id = id;
        return this;
    }

    public String getMemberId() {
        return memberId;
    }

    public CardHolder setMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public CardHolder setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CardHolder that = (CardHolder) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CardHolder.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("memberId='" + memberId + "'")
                .add("cardNumber='"
                        + (cardNumber != null
                                ? "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4))
                                : null)
                        + "'")
                .toString();
    }
}
