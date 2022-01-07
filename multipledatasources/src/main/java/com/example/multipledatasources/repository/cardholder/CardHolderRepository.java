package com.example.multipledatasources.repository.cardholder;

import com.example.multipledatasources.model.cardholder.CardHolder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardHolderRepository extends JpaRepository<CardHolder, Long> {
    
}
