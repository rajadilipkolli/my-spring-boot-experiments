package com.example.multipledatasources.model.cardholder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class CardHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String memberId;
    private String cardNumber;

}
