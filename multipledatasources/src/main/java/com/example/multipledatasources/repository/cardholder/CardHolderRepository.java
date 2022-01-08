package com.example.multipledatasources.repository.cardholder;

import com.example.multipledatasources.model.cardholder.CardHolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardHolderRepository extends JpaRepository<CardHolder, Long> {

  Optional<CardHolder> findByMemberId(String memberId);
}
